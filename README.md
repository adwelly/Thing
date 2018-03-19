Thing: An interactive data explorer for Clojure

Thing is prototype library for the interactive exploration of *large* Clojure data structures. 

Despite TDD, I still find myself having to debug Clojure programs and in my case this usually involves printing out the value of various forms. When the values are relatively small, this usually needs nothing more than some variation on:

```clojure
(defn spy [val] (do (println val) val)
```

Which can be wrapped around the form to print out its value during execution.

This falls down completely when faced with a substantial data structure - one that would produce pages of text when pretty printed. For this situation I now have Thing which will print out a partial description of the data under examination and will then allow you to move around focusing on sub parts until you are satisfied that you understand what has been produced. Quitting Thing returns the value and standard code execution can then resume.

You invoke Thing with the debug function:

```clojure
(debug [[1 2 3 4 5] (list "this" "that" "the other" "something else" "yet more") :a :b :c])
```

Which will produce:

```
0.  [[1 2 3 4 ...] 
1.   ("this" "that" "the other" "something else" ...)
2.   :a 
3.   :b 
4.   :c ]
[] -> 
```

If you enter 0:

```
0.  [1 
1.   2 
2.   3 
3.   4 
4.   5 ]
[0] -> 
```

The currently available commands are:

1. \x - exit.
2. \h - print this message.
3. .. - pop the path.
4. <num> - examine that part of the structure.

Thing distinquishes between lists (printed between `(` and `)` and sequences, that are not necessarily countable which are printed between `<` and `>`.



