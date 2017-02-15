Active-MQ with Spring in Openshift Walkthrough
==============================================

Note: It is recommended to check out the [Openshift](https://github.com/mongoos2006/monkeybrainsdevops/wiki/Lesson-Openshift) lesson first before proceeding.

This is a demonstration of deploying 3 resources into Openshift: a Java Active-MQ consumer and producer, and an Active-MQ broker.  
We will use the broker container that is already inside the registry but we will build the consumer and producer.
You will need sudo access to your machine if you need to install the certificates when using a secured Docker registry.  

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

You should see a success message as well as a message indicating to create a new project if one doesn't exist already.

### Create a new OS project (namespace) to work in
Choose a `$PROJECT_NAME` to remember and replace it in the command below.

```
oc new-project $PROJECT-NAME
```

### Configure Test Nexus Instance <-> Project
**For testing only** This uses an insecure Docker registry.  

```
oc secrets new-dockercfg secret_name --docker-server=@--IP-ADDRESS-OMITTED--@ --docker-username=username --docker-password=password --docker-email=DOESNT_MATTER@BLAH.BLORP
oc secrets link default secret_name --for=pull
```

### Build and containerize new applications
NOTE!!:This section uses a private Docker registry (Nexus). If the machine you are using to build this application is not set up to communicate with the registry you will need to follow these steps first (need openssl and `$JAVA_HOME` set):

```
echo -n | openssl s_client -connect @--IP-ADDRESS-OMITTED--@:443 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/@--IP-ADDRESS-OMITTED--@.crt
sudo $JAVA_HOME/bin/keytool -import -noprompt -trustcacerts -alias nexus-gss -file /tmp/@--IP-ADDRESS-OMITTED--@.crt -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit
sudo reboot
```

Here we will pull the code for the consumer/producer, dockerize it and push it to the test registry. This assumes you have SSH keys set up in github.

```
git clone git@git.somewhere/amq-spring-demo.git
cd amq-spring-demo
```

Now we can build each app.  
NOTE!!:This part will require you to add to your `DOCKER_OPTS` the registry and the fact that it's an insecure registry if you have not done so already. There is a lot of information on the internet regarding this if you don't know how to.

First we will build the producer:
```
cd producer
chmod +x gradlew
./gradlew build
mv build/libs/messaging-jms-0.1.0.jar .
sudo docker login -u admin -p admin123 -e @ @--IP-ADDRESS-OMITTED--@
sudo docker build -t @--IP-ADDRESS-OMITTED--@/jms-producer:latest .
* sudo can be omitted if you've already added your user to the docker user group 
```
Then build the consumer:
```
cd ../consumer
chmod +x gradlew
./gradlew build
mv build/libs/messaging-jms-0.1.0.jar .
sudo docker build -t @--IP-ADDRESS-OMITTED--@/jms-consumer:latest .
```
Check your images:
```
sudo docker images
```
`jms-producer` and `jms-consumer` should be present.

### Push both containers to the registry
```
sudo docker push @--IP-ADDRESS-OMITTED--@/jms-consumer:latest
sudo docker push @--IP-ADDRESS-OMITTED--@/jms-producer:latest
```

### Create the producer in OpenShift from the UI
There are many ways to deploy into OpenShift. This part will cover deploying from the web console UI.  
First we need to create an imagestream for OpenShift so that OpenShift knows where to find the image.  
Double check that you are on the project you specified earlier.
```
oc project
```
You should see `$PROJECT-NAME` from earlier.
Then create your imagestream in OpenShift so that OpenShift knows where to find your container.
```
oc import-image producer --from=@--IP-ADDRESS-OMITTED--@/jms-producer:latest --confirm --insecure
```
This should complete successfully.

### Go to the OpenShift UI and configure the system to deploy
Go to the OpenShift web console, and log in with your credentials `$USERNAME` and `$PASSWORD`
```
https://@--IP-ADDRESS-OMITTED--@/console/
```
Click on your project `$PROJECT-NAME`  
You should see "Get started with your project."  
Click on the add to project button  
```
click "Add to Project"
```
At the top click on `Deploy Image`
```
click "Deploy Image"
```
Under "Image Stream Tag" select your `$PROJECT-NAME`  
Under "Image Stream" select `producer`  
Under tag select `latest`  
Under "Environment Variables" enter the following sets by clicking `add environment variable` for each:
```
SPRING_ACTIVEMQ_USER=admin1234
SPRING_ACTIVEMQ_PASSWORD=1234admin
QUEUE_ONDEMAND_NAME=queue.onDemand
QUEUE_STREAM_NAME=queue.hello
SPRING_ACTIVEMQ_BROKER_URL = tcp://activemq:61616
SERVER_PORT = 8080
```
Then click `Create`
```
click "Create"
```
Click on `Continue to overview` to view your app deploying.

### View the server logs
On the left of the web concole click on applications
```
click "Applications"
```
Then click `Pods`
```
click "Pods"
```
There should be a pod called `producer-1-xxxxx`
```
click "producer-1-xxxxx"
```
Then click "Logs" tab
```
click "Logs"
```
At the bottom you should see the message `Tomcat started on port(s): 8080 (http)` indicating the application is active

### Launch the Active-MQ service
At this point the application should be running and all we have to do is launch the Active-MQ broker and the consumer.  
We will first launch the broker. We will do this with an existing Active-MQ image inside the registry already.
```
oc import-image activemq --from=@--IP-ADDRESS-OMITTED--@/jboss-amq-6/amq62-openshift:latest --confirm --insecure
oc new-app -i activemq -e MQ_USERNAME=admin1234 -e MQ_PASSWORD=1234admin -e MQ_QUEUES=queue.onDemand,queue.stream
```
This will automatically create all the configuration required to have an AMQ broker as well as set the environment variables needed to connect to the producer and consumer.


### Expose the producer app so we can see the UI
We want to test the connection between the producer and the broker so lets open up the correct services and routes so we can access the front end of the producer application.  
We need to create a route so that it can talk outside the entire OpenShift cluster (so we can see the front end).
Replace `$UNIQUE_HOSTNAME` with something unique to the project
```
oc expose service producer --hostname=$UNIQUE_HOSTNAME --port=8080
```
Now open your browser and go to `$UNIQUE_HOSTNAME`
```
go to "$UNIQUE_HOSTNAME"
```
You should see a simple UI with an option at the top to "send a string". Send a couple of random things (the UI doesnt reset after each push of the send button).  
Now lets check on the server logs for the producer and see if its being sent properly.  
Go to the console and project overview again.
```
go To "https://@--IP-ADDRESS-OMITTED--@/console/"
```
```
click "Applications" on the left menu of the web console then
click "Pods"
```
You should see one of the pods called "producer-x-xxxxx"
```
click `producer-x-xxxxx` then
click the "Logs" tab and scroll to the bottom
```
You should see `POST REQUEST FOR /sendString params:` for each time you press the send button.


### Deploy the consumer app
Now that we can put messages into the broker, let's try to consume them with a separate application.  
We already pushed the image to the Nexus repository so all we need to do is deploy it and change a few network configurations.  
This is the same process for the producer but this time we can do it all from the command line.  
Go back to the developer machine with `oc` installed.  
Create the image stream:
```
oc import-image consumer --from=@--IP-ADDRESS-OMITTED--@/jms-consumer:latest --confirm --insecure
```
Create the new OpenShift resource based on the image stream created. This will include all of the environment variables at once.
```
oc new-app -i consumer -e SERVER_PORT=8080 -e \
SPRING_ACTIVEMQ_BROKER_URL=tcp://activemq:61616 -e \
SPRING_ACTIVEMQ_USER=admin1234 -e \
SPRING_ACTIVEMQ_PASSWORD=1234admin -e \
QUEUE_ONDEMAND_NAME=queue.onDemand -e \
QUEUE_STREAM_NAME=queue.hello
```
Go to the web console overview and see if the new app builds. once its finished come back here.  
Expose a route for external communication.  
Make up another unique hostname `$ANOTHER_UNIQUE_HOSTNAME`.
```
oc expose service consumer --hostname=$ANOTHER_UNIQUE_HOSTNAME --port=8080
```

### Open consumer app and pull messages
Now open a browser and go to $ANOTHER_UNIQUE_HOSTNAME and see the running consumer.
```
open browser to "http://$ANOTHER_UNIQUE_HOSTNAME"
```
Go to retrieve a string and click `Get String`. the app should start pulling messages from the broker one at a time.
```
click "Retrieve A String"
click "Get String"
```
all done :)


Now clean up your mess:
```
oc project # make sure you're still on your project
oc delete all --all
```
