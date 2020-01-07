# TimetableBot

This application sends changes of your timetable as a [Telegram](https://telegram.org/) message.

It works with [Untis](https://www.untis.at) and currently all strings are hardcoded in German, 
but feel free to change the software according to your needs.

## Installation

First you have to get Telegram bot name and token.
You can request this from the [BotFather](https://core.telegram.org/bots#3-how-do-i-create-a-bot).

Now you've got two options to install the TimetableBot:

### Running the Jar file

*This should work on every system where you can install Java 11*

1. Install Java 11 or higher
2. Install MySQL or MariaDB
3. Download the [latest release](https://github.com/timetablebot/bot/releases) and extract it
4. Edit the configuration file in the `config` folder. You have to change the database settings, 
insert your telegram name and token and add a Base64 authentication for the timetable.
5. Change the name of the configuration file from `example.config.ini` to `config.ini`
6. Run the application via `java -jar timetablebot.jar`

### Using Docker Compose

*This only works on Amd64 Linux*

1. Install [docker](https://docs.docker.com/install/linux/docker-ce/debian/)
2. Install [docker-compose](https://docs.docker.com/compose/install/)
3. Run the following commands to download the [docker-compose.yml](https://raw.githubusercontent.com/timetablebot/bot/master/docker-compose.yml) file and the configuration
```bash
# Create a new directory 
mkdir timetablebot && cd timetablebot/

# Download the docker-compose.yml
curl -LO https://raw.githubusercontent.com/stundenplanbot/bot/master/docker-compose.yml

# Create a new directory for the configuration
mkdir config && cd config/

# Download the default configuration
curl -LO https://raw.githubusercontent.com/stundenplanbot/bot/master/config/example.docker-compse.config.ini

# Copies the configuration if it's non-existent
cp -n example.docker-compse.config.ini config.ini
```
4. Edit the configuration 
5. Start docker compose via `docker-compose up`

## Update

### Running the Jar file

1. Download the [latest release](https://github.com/timetablebot/bot/releases) and extract it
2. Replace the `timetablebot.jar` file
3. See if the [`example.config.ini`](https://github.com/timetablebot/bot/blob/master/config/example.config.ini) has changed and add new configuration options 
to your `config.ini` to prevent a error at startup.

### Using Docker Compose

1. Download a updated docker compose file
1. Run `docker-compose pull` to update the images
2. See if the [`example.docker-compose.config.ini`](https://github.com/timetablebot/bot/blob/master/config/example.docker-compse.config.ini) has changed and add new configuration options 
to your `config.ini` to prevent a error at startup.

## Post-Installation

### All Commands

Here's a list of all command you can run in Telegram.
You can also get this list by running `/helpcmd`.
If you don't have administrative rights the list may be shorter.

* `/broadcast` - (ADMIN) Send a message to all of your users
* `/deletedata` - Deletes all of the users data
* `/feedback` - Allows users to voice their feedback
* `/grade` - Changes your grade, so that you only get the correct timetable changes
* `/help` - Shows a help menu and a description
* `/helpcmd` - Gets this list
* `/mensa` - Shows the current cafetiera meals
* `/msguser` - (ADMIN) Send a message to one user
* `/news` - Shows the news atop of the timetable 
* `/newschoolyear` - (ADMIN) Send a message to all of your users with details about the new school year
* `/panicshutdown` - (ADMIN) Shutdown the bot if something is goes wrong
* `/refreshmensa` - (ADMIN) Reloads the cafetiera meals from the database
* `/tokens` - Shows all shortcut for teachers used in the timetable


### Get administrative rights

*Start here if you use docker compose*

If you use Docker compose you can enter your database server with `docker-compose exec mariadb sh`.
Login into your mysql server with the credentials given in your `docker-compose.yml` file using
`mysql -uroot -Dtimetablebot -p`. Enter the given password (defined with `MYSQL_PASSWORD`) when promted. 

*Start here if you installed MySQL or MariaDB by yourself and login into your SQL server*
1. Show all users using `SELECT * FROM users;`. 
2. Search for your Telegram name (column `name`) and write down the `chatid`
3. Run ``UPDATE users SET `rank` = 'ADMIN' WHERE chatid = [INSERT YOUR CHATID HERE];``
4. Now you've got admin rights. Check the result with `SELECT * FROM users;`. You `rank` should be `ADMIN`.
5. Restart the bot. If your running it via docker you can use `docker-compose restart ttbot`.


### Display the cafeteria plan

Via the `/mensa` command you can show the cafertia meals for the next two days.
This only works automatically if the `CafertiaParser` works as intendend, 
but this isn't always the case, 
so you can scan the plan by yourself and upload the meals for the week.

To do this you can use the [scanner app](https://github.com/timetablebot/scanner) 
and the [scanner api](https://github.com/timetablebot/scanner_api).