# My Resume

This project generates my CV as a static web site, containing only HTML and CSS, using a set of Markdown files as input.

## Requirements

- JDK/JRE 1.7.0 or later
- Boot CLJ
- Firebase CLI

If installing the JDK on Windows via scoop, Boot will not be able to find the installation on your system. To remedy this, there is a script containing a registry fix at the root of this project called `scoop_boot_java_fix.reg`. You will need to edit script with the version and path details appropriate for your system, then open regedit and import it.

## Usage

To build the static output:
```
boot once
```

To build, serve locally on port 3000 and automatically re-build when source changes:
```
boot dev
```

To publish to Firebase:
```
boot once
firebase login # Only required once per user profile
firebase deploy
```

## Goals

- The resulting static web site should be clean and readable
- When printed, the result should still be clean and readable
- It should be trivial to edit existing sections and add new section

## TODO

- Move from Boot to CLJ Command Line Tools
- Revise use of Garden and compilation of CSS