
                _..---...,""-._     ,/}/)
             .''        ,      ``..'(/-<
            /   _      {      )         \
           ;   _ `.     `.   <         a(
         ,'   ( \  )      `.  \ __.._ .: y
        (  <\_-) )'-.____...\  `._   //-'
         `. `-' /-._)))      `-._)))
           `...'
         _       _   ___                     _
     ___| |_ ___| |_| | |_ _ ___ ___ ___ ___| |
    | . | . | . |  _|_  | | |   |  _| -_| .'| |
    |_  |___|___|_|   |_|___|_|_|_| |___|__,|_|
      |_|
    ___        __    __         _                        __  __
     | |_  _  /  \  |__) _ |_  (_ _  _  /  \ _  _ _ _ |||__)/   _|
     | | )(-  \_\/  |__)(_)|_  | (_)|   \__/| )| (-(_|||| \ \__(_|

# The Q Bot for UnrealIRCd

## What is this

This is a small personal project with the purpose to "port" the Q bot 
that can be used on QuakeNet (snircd/ircu daemon), to UnrealIRCd daemon.

## Why is that

I kinda like the Q bot (I find it just as enough for channel management).
I also kinda like the features of UnrealIRCd (very modular, also has some
interesting security features).
I don't like the services provided by Anope/Atheme (too many features,
too many nicks) and ircu (too few of anything).

So the idea is to render the Q bot for UnrealIRCd networks, and also adding features
that would not be possible with ircu (SASL for example).

Project made in Java to help me to get back into it.

## Features

Plan for features to be (more or less) herited from the Q bot:
* account registration
* account auth (password, challenge)
* user auto vhost on auth
* no nick ownership
* account properties/rights (through flags)
* straightforward channel registration (not through R, or why not)
* channel chanlev, chanflags system
* channel things (topic, modes, bans...)
* somer opers stuff

Plan for some other features:
* modern auth system (certificate, sasl), made possible by UnrealIRCd
* mlock management, made possible by UnrealIRCd
* maybe nick ownership option
* maybe some other things I like from anope/atheme


## Roadmap

There is no roadmap, the development just goes as my ideas flow, but I have created a [To Do](TODO.md) list.

## License

GNU General Public License v2

## Contact

Project is hosted at irc.mjav.xyz / #qbot

## How to use

% javac *.java

% java QBot
