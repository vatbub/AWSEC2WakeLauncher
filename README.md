# AWS EC2 Wake Launcher
Did you ever find yourself in this situation:
- You have a server that needs to run on AWS EC2.
- The server does not have that much traffic, but...
- ...running the server costs a heck of money.

Here is where this project comes into play. WakeLauncher is a tomcat webapp that can put your EC2 instance to sleep and wake it up once you need it again.

## But wait! You said its a web app, so I need a Tomcat server for it!
You are right, but: [Heroku](https://www.heroku.com/) offers free and simple hosting for web apps.

## How it works
1. Add the application client to your application [WIP]
2. Add the server client to your server application [WIP]
3. Whenever you want to request something from your EC2 instance, you first call the application client. The application client then pings WakeLauncher. If your instance is already running, WakeLauncher will tell you and you may proceed with the actual request. If the instance is not running, WakeLauncher will boot it and tell you when it's ready. 
4. Whenever your server is idle, it notifies the server client about that. After a specified timeout, the server client then reports to WakeLauncher that the instance may go to sleep. WakeLauncher will then shut the instance down.

## Some things to keep in mind
- WakeLauncher requires you to have an instance already configured. 
- Your server application must start automatically when the instance boots up, WakeLauncher cannot issue any shell commands on the instance.

## Download
I just started this project, give the repo a star and click the `Watch` button and you will be the first one to be notified about the launch.

## Usage
### Application client
#### Java
Add the following maven dependency [WIP]

You may now do the following:

```java
import com.github.vatbub.awsec2wakelauncher.applicationclient.Client;

public class Sample{
	public void sample(){
		Client client = new Client (new URL("localhost"));
		launchAndWaitForInstance(instanceId);
	}
}
```

#### JSON interface
When using any other programming language, you may interact with WakeLauncher using its JSON interface. [WIP]
