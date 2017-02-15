# Working with OpenShift
This is a short preview of deploying a couple containers into openshift. It does this in 2 parts. One is deploying an already configured template from the openshift user interface to get a look at the OS UI. The other is creating a brand new image from source code, creating a new template, and from the CLI deploying the application. It currently uses a nexus repository stood up for testing OS, however it can be configured to use any registry.


### Downloading OC Tools
We need the OS Client Tools to be able to interact with the remote openshift. From a "developer" machine (gradle, docker, oc tools, git) we will first need to download the tools package and add to path or reference it for `OC` commands.
This may require the use of a USCIS proxy to download from github
```
wget https://github.com/openshift/origin/releases/download/v1.3.2/openshift-origin-client-tools-v1.3.2-ac1d579-linux-64bit.tar.gz
tar -xvf openshift-origin-client...
```

### Log into OpenShift from CLI
You should see a success message as well as a message indicating to create a new project. Use the `$USERNAME` and `$PASSWORD` given to you.
```
oc login https://@--IP-ADDRESS-OMITTED--@ -u $USERNAME -p $PASSWORD
```


### Create a new OS project (namespace) to work in
make up a `$PROJECT_NAME` to remember and replace in the command below.
```
oc new-project $PROJECT-NAME
```

### Configure Test Nexus Instance <-> Project
TESTING ONLY docker registry
```
oc secrets new-dockercfg nexussecret --docker-server=@--IP-ADDRESS-OMITTED--@ --docker-username=admin --docker-password=admin123 --docker-email=DOESNT_MATTER@BLAH.BLORP
oc secrets link default nexussecret --for=pull
```

### Create a Postgres DB pod from the OS console UI
Go to `https://@--IP-ADDRESS-OMITTED--@/console` with your browser and log in with `$USERNAME` and `$PASSWORD`.  
1. click `$PROJECT_NAME`  
2. click "add to project" to add a new resource  
3. filter the list by "postgresql-ephemeral" to find the configuration for a postgres instance  
4. in the options change "PostgreSQL Connection Username", "PostgreSQL Connection Password", and "PostgreSQL Database Name" to: `postgres`  
5. click create at the bottom  
6. click `continue to overview` to go to the main project overview page  
7. the postgres pod should boot up from the preloaded image configuration  


### Get application code
On the developer machine again, clone the repository and move into its directory.
This assumes you have keys set up for github.
```
git clone git@git.uscis.dhs.gov:USCIS/pg-spring-demo.git
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
This assumes you want to use our test Nexus Registry. Additional steps may need to be performed if you would like to use a different registry. Build, move to docker folder, chmod jar to 664, build image/tag, push to nexus.
need gradle installed.
Remember to replace `$APPLICATION_NAME` with your application name
```
gradle build -x test
mv ./build/libs/pg-spring-demo-0.1.0.jar docker/
chmod 664 docker/*.jar
sudo docker login -u admin -p admin123 -e @ @--IP-ADDRESS-OMITTED--@
sudo docker build -t '@--IP-ADDRESS-OMITTED--@/$APPLICATION_NAME:latest' ./docker
sudo docker push @--IP-ADDRESS-OMITTED--@/$APPLICATION_NAME:latest
```

### Add the template to OpenShift and then create a new app based on that template
Add the template modified earlier `pg-spring-demo`, then tell OS you want to create/init the resources specified in the template with `new-app`
remember to replace `$PROJECT_NAME` with your project name
```
oc create -f pg-spring-demo
oc new-app --template="$PROJECT_NAME/pg-spring-demo"
```

### Check on the build
Go back to the openshift UI Console project overview and you should see both pods building/running. At the top right there should be a link `$SUB_DOMAIN_NAME`.michaeldd.net. Click this link and it should take you to the working application.

### Continue to build application
From this point you can continue to change code and rebuild the image, push to nexus. After any changes you will need to tell openshift to re-deploy using the new image with the `import-image` command:
```
oc import-image pg-spring-demo-i-stream:latest --from=@--IP-ADDRESS-OMITTED--@/$APPLICATION_NAME:latest --confirm --insecure
```
This can obviously be configured to automatically rebuild.
