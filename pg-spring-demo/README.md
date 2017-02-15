Postgres with Spring in Openshift Walkthrough
==============================================

Note: It is recommended to check out the [Openshift](https://github.com/mongoos2006/monkeybrainsdevops/wiki/Lesson-Openshift) lesson first before proceeding.

# Working with Openshift
This is a short preview of deploying a couple of containers into Openshift. It does this in 2 parts. One is deploying an already configured template from the Openshift user interface to get a look at the OS UI. The other is creating a brand new image from source code, creating a new template, and from the CLI deploying the application. It currently uses a Nexus repository for testing purposes, but it can be configured to use any registry.


### Downloading OC Tools
We need the Openshift client tools to be able to interact with the remote Openshift cluster. From your local machine we will first need to download the tools package and add it to your PATH or reference it for `oc` commands.  
Skip this part if you already have openshift tools installed.  
This is currently included in the repository for Linux users:

```
cd amq-spring-demo #Wherever you downloaded the repo
tar -xvf openshift-origin-client-tools-v1.4.1-3f9807a-linux-64bit.tar.gz
* Version subject to check
```
Then add the binaries to your PATH (e.g. bash_profile).

* For other platforms, download the CLI tools from the [Origin project](https://github.com/openshift/origin/releases)

### Log into OpenShift from CLI
Use the `$USERNAME` and `$PASSWORD` given to you.
```
oc login https://@--IP-ADDRESS-OMITTED--@ -u $USERNAME -p $PASSWORD
```
You should see a success message as well as a message indicating to create a new project. 

### Create a new OS project (namespace) to work in
Create a `$PROJECT_NAME` and replace it in the command below.
```
oc new-project $PROJECT-NAME
```

### Configure Test Nexus Instance <-> Project
Note: The registry is for testing purposes only
```
oc secrets new-dockercfg secret_name --docker-server=@--IP-ADDRESS-OMITTED--@ --docker-username=username --docker-password=password --docker-email=DOESNT_MATTER@BLAH.BLORP
oc secrets link default secret_name --for=pull
```

### Create a Postgres DB pod from the OS console UI
Go to `https://@--IP-ADDRESS-OMITTED--@/console` with your browser and log in with `$USERNAME` and `$PASSWORD`.  
1. Click `$PROJECT_NAME`  
2. Click "add to project" to add a new resource  
3. Filter the list by "postgresql-ephemeral" to find the configuration for a postgres instance  
4. In the options change "PostgreSQL Connection Username", "PostgreSQL Connection Password", and "PostgreSQL Database Name" to: `postgres`  
5. Click create at the bottom  
6. Click `continue to overview` to go to the main project overview page  
7. The postgres pod should boot up from the preloaded image configuration  


### Get application code
On the developer machine again, clone the repository and move into its directory.
This assumes you have keys set up for github.
```
git clone git@github.com:mongoos2006/monkeybrainsdevops.git
cd pg-spring-demo
```

### Modify the OS Template (app configuration) to be unique
Modify the APPLICATION_NAME and SUB_DOMAIN_NAME __**values**__ at the top of the file `pg-spring-demo` to some unique value inside the template configuration. Non-unique values runing simultaneous in OS will probably throw an error.
`vi pg-spring-demo`:
```
- description: The name for the application.
  name: APPLICATION_NAME
  value: *some-application-name*
  required: true

- description: subdomain.
  name: SUB_DOMAIN_NAME
  value: *some-sub-domain*
  required: true
```
remember `$APPLICATION_NAME` and `$SUB_DOMAIN_NAME`


### Build with Gradle, Push to Nexus Registry
This assumes you want to use our test Nexus Registry. Additional steps may need to be performed if you would like to use a different registry. Build, switch to the docker folder, chmod the jar to 664, build image/tag, push to nexus.
Gradle must be installed.
Remember to replace `$APPLICATION_NAME` with your application name
```
gradle build -x test
mv ./build/libs/pg-spring-demo-0.1.0.jar docker/
chmod 664 docker/*.jar
sudo docker login -u username -p password -e @ @--IP-ADDRESS-OMITTED--@
sudo docker build -t '@--IP-ADDRESS-OMITTED--@/$APPLICATION_NAME:latest' ./docker
sudo docker push @--IP-ADDRESS-OMITTED--@/$APPLICATION_NAME:latest
* sudo can be omitted if you've already added your user to the docker user group 
```

### Add the template to OpenShift and then create a new app based on that template
Add the template modified earlier `pg-spring-demo`, then tell Openshift you want to create/initialize the resources specified in the template with `new-app`
Remember to replace `$PROJECT_NAME` with your project name
```
oc create -f pg-spring-demo
oc new-app --template="$PROJECT_NAME/pg-spring-demo"
```

### Check on the build
Go back to the Openshift UI console Project Overview page and you should see both pods building/running. At the top right there should be a link `$SUB_DOMAIN_NAME`. Click this route and it should take you to the working application.

### Continue to build application
From this point you can continue to change code and rebuild the image, push to Nexus. After any changes you will need to tell Openshift to re-deploy using the new image with the `import-image` command:
```
oc import-image pg-spring-demo-i-stream:latest --from=@--IP-ADDRESS-OMITTED--@/$APPLICATION_NAME:latest --confirm --insecure
```
This can be configured to automatically rebuild through the DeploymentConfig.
