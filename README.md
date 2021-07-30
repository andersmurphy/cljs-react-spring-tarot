## Dependencies

Install dependencies:

`yarn install`

## Repl

Start a repl:

`yarn shadow-cljs clj-repl`

Run the watch command:

`(shadow/watch :app)`

Open your browser and got to:

`http://localhost:8000/`

Switch to cljs repl:

`(shadow/repl :app)`

Eval some code:

`(js/alert "foo")`

If get this message after evaluating some code:

`There is no connected JS runtime.`

You need to open your App in the browser at:

`http://localhost:8000/`

You can quit the cljs repl and return to the shadow repl with:

`:repl/quit`

## Documentation

[shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html)
