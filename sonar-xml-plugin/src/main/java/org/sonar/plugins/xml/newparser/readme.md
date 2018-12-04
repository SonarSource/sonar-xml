
Sets location ranges on `Document` nodes as user data. See `Document#getUserData(String)` to retrieve these data.
Each node has `NODE` storing the whole node range.
Then depending on a node type, other kinds of locations might be available.

### Simple element
```
<foo> </foo>
 ^^^            NAME
^^^^^           START
      ^^^^^^    END
^^^^^^^^^^^^    NODE
```

### Self-closing element
```
<foo />
 ^^^         NAME
^^^^^^^      START
^^^^^^^      END
^^^^^^^      NODE
```

### Attribute
```
attr = "hello"
^^^^            NAME
       ^^^^^^^  VALUE
^^^^^^^^^^^^^^  NODE
```

### Text
```
<foo>Hello world</foo>
     ^^^^^^^^^^^            NODE
```

### Comment
```
<foo><!-- comment --></foo>
     ^^^^^^^^^^^^^^^^            NODE
```

### CDATA
```
<![CDATA[ ... ]]>
^^^^^^^^^             START
              ^^^     END
^^^^^^^^^^^^^^^^^     NODE
```

### DOCTYPE
```
<!DOCTYPE ... >
^^^^^^^^^           START
              ^     END
^^^^^^^^^^^^^^^     NODE
```

### Prolog (see `PrologElement` API)
```
<?xml version="1.0" ?>
^^^^^                  start
      ^^^^^^^          attribute name
              ^^^^^    attribute value
                    ^^ end
```
