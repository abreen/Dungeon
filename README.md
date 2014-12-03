# Dungeon

![Logged into a server](https://raw.githubusercontent.com/abreen/Dungeon/master/screenshot.png)

`Dungeon` is a multiplayer text adventure game engine written in Java. It
is inspired by traditional text adventure games from years ago, with some
new ideas and features.

This project comes with a server and client. See the `USAGE.md` file for
more information about how to use them.

`Dungeon` is my first exploration into text-based games and network
programming. You're welcome to help out with development.

There is no "license" for the source code, apart from the requirement that
you don't sue me if it breaks anything. Expect me to find an appropriate
license in due time.


## Extensibility

Since `Dungeon` is a game *engine*, you can write your own universe files
and start the `Dungeon` server with them. See the `yaml` directory for
the configuration file (to tell the server which universe file to
load) and the default "demo" universe file that I've written.

## Dependencies

The server uses [SnakeYAML][snakeyaml], a YAML parser for Java. The JAR
file (version 1.12) is included in the `lib` directory.

The client uses [Lanterna 3][lanterna], a pure Java terminal UI library
that's super cool, and is included in the `lib` directory.


## Building and running

See `USAGE.md`.

[snakeyaml]: http://code.google.com/p/snakeyaml/
[lanterna]: http://code.google.com/p/lanterna/
