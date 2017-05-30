# Airline Ticket Reservation System (ATRS w/ JMS)

This is a reference application for developers to learn how to build applications with Macchinetta Framework.

## How to run the application

### Download source code

Download source code from [here](https://github.com/Macchinetta/atrs-jms/releases "here").
Extract the zip file at any location of choice.

### Install PostgreSQL

Install and start PostgreSQL.
Select 'postgres' as password for postgres user.
If you'd like to use other password, some changes will be required in setting files.

### Insert test data

First of all, create database named 'atrs'.

After that, execute the command below at the directory where the downloaded source code is unzipped.

```console
$ mvn -f atrs-initdb/pom.xml sql:execute
```

It is assumed that maven is already installed.
If not, install it and try again.

### Install ActiveMQ

Install and start ActiveMQ.
Access [http://localhost:8161/admin]. 
The default username and password are both 'admin'.
Create a queue named 'ATRS.D1_HISTORY_REPORT.QUEUE'.

### Install and setting Tomcat

Install Tomcat.

Copy 'activemq-client-5.x.x.jar' and its dependencies from ActiveMQ lib directory to Tomcat lib directory.
When you use ActiveMQ 5.14.5, the files listed below are required in tomcat lib directory.
* activemq-client-5.14.5.jar
* geronimo-jms_1.1_spec-1.1.1.jar
* geronimo-j2ee_management_1.1_spec-1.0.1.jar
* hawbuf-1.11.jar
* slf4j-api-1.7.13.jar

Finally, add resource settings to 'conf/context.xml'.

```XML
<Context>

    <!-- omitted -->

    <Resource name="jms/ConnectionFactory" auth="Container"
        type="org.apache.activemq.ActiveMQConnectionFactory"
        description="JMSConnectionFactory"
        factory="org.apache.activemq.jndi.JNDIReferenceFactory"
        brokerURL="tcp://localhost:61616?jms.redeliveryPolicy.maximumRedeliveries=1"
        brokerName="ActiveMQBroker" useEmbeddedBroker="false"/>

    <Resource name="jms/queue/ReservationHistoryReportRequestQueue"
        auth="Container"
        type="org.apache.activemq.command.ActiveMQQueue"
        factory="org.apache.activemq.jndi.JNDIReferenceFactory"
        physicalName="ATRS.D1_HISTORY_REPORT.QUEUE"/>

</Context>
```

### Build applicatoin

Execute the command below at the directory where the downloaded source code is unzipped.

```console
$ mvn install
```

Deploy 'atrs-web/target/atrs.war' to tomcat and access [http://localhost:8080/atrs].
