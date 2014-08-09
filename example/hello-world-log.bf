Store log(String) lambda

+.          STORE_LAMBDA
>..         x0000 = plugin address

>           l
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++.

+++.        o

--------.   g

>,          store lambda address
>,

Save string "Hello World"

>           previous two cells contain lambda address
+++++.      STORE_STRING


>           H
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++.

            e
+++++++++++++++++++++++++++++.

+++++++..   ll
+++.        o

>           ' '
++++++++++++++++++++++++++++++++.

            W
+++++++++++++++++++++++++++++++++++++++++++++++++++++++.

            o
++++++++++++++++++++++++.

+++.        r

------.     l

--------.   d

>,          store string address
>,

Invoke lambda log(String) with argument "Hello World"

>           previous two cells contain string address
.           INVOKE_LAMBDA

<<<<<<<.>.  send lambda address
>>>>.>.     send string address

,,          Invoke
