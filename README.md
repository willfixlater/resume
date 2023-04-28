# My Resume

This project generates my CV as a static web site, containing only HTML and CSS, using a set of Markdown files and Garden (CSS-in-EDN) docs as input.

## Requirements

- JDK 7 or later
- Clojure CLI
- Firebase CLI

## Usage

From the REPL, you can run the following functions in `resume.main`.

To build the static output:
```
(build!)
```

To build, serve locally on port 3300 and automatically re-build when source changes:
```
(start-serve-&-watch default-opts)
```

To stop the dev server and source watchers:
```
(stop-serve-&-watch default-opts)
```

To build & publish to Firebase, run the following from terminal:
```
clj -X resume.main/build!
firebase login # Only required once per user profile
firebase deploy
```

## Goals

- The resulting static web site should be clean and readable
- When printed, the result should still be clean and readable
- It should be trivial to edit existing sections and add new section
- Serve as a testing ground for my ideas around Static Site Generation (SSG) & Server Side Rendering (SSR)

## TODO

- Fix hacks and viewport bugs in CSS
- Add exception handling when reloading source for dev
- Revise use of Garden and compilation of CSS