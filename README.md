# LOX24 SMS Notification for Jenkins

This is Jenkins plugin that sends SMS notification whenever there's a failed build. The plugin is powered by [LOX24][homepage]

### Get LOX24 Account

Visit [LOX24][homepage] site to get a LOX24 account.

### Setup

Go to Manage Jenkins -> Configure System, navigate to "LOX24 Credentials" and fill in the konto, password and service of your LOX24 account

## Usage

To setup SMS notification for a specific Jenkins job, go to Configuration of the job.
Then add a Post-build task named "LOX24 SMS Notification", fill in the mobile numbers you want to retrieve the SMS with the following format:+6591234567
Multiple numbers are supported by adding a comma "," between the numbers.


## LOX24 API

Visit [documentation] to learn more about the LOX24 API.


[homepage]:https://www.lox24.eu/
[documentation]:http://www.lox24.eu/api/LOX24-SMS-API-en.pdf
