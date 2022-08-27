# Java Jukebox

Use this program to shuffle your favourite tracks.

## Installation

To run this, you will need to install a [java compiler](https://www.java.com/en/download/help/linux_install.html), e.g. with:

```
sudo apt update
sudo apt install default-jre
sudo apt install default-jdk
javac -version
```

Additionally, you will need a music player. The Jukebox is hard-coded to use `mpg123` player, which you can install on Ubuntu with:

```
sudo apt install mpg123
```


## Run it

You can compile the program from the root directory with:

```
javac Jukebox.java
```

Then run it:

```
$ java Jukebox ~/Music/
Now playing: 01 - Miami 82 (feat. Madame Buttons) (Vocal Radio Edit)
Location   : ~/Music/workout_music/01 - Miami 82 (feat. Madame Buttons) (Vocal Radio Edit).mp3
Now playing: 04 - Giving It All (Friend Within Remix)
Location   : ~/Music/workout_music/04 - Giving It All (Friend Within Remix).mp3
Now playing: 01 - We Are Love
Location   : ~/Music/workout_music/01 - We Are Love.mp3
...
End of playlist.
```

There is skipping capability (runs on a separate thread), which you can use to jump to the next track: just tap "s" followed by Enter.
