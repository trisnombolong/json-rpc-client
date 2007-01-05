JSON-RPC-Client
===============
A client for JSON-RPC written in Java.

Author
======
Sasha Ovsankin <sasha@codebistro.com>


Building
========
Dependencies:

	1. JSON-RPC from MetaParadigm found at http://oss.metaparadigm.com/jsonrpc/.
	2. 3 Apache-commons libraries found in lib/ directory. Go to http://jakarta.apache.org/commons/
	   for more details.

The distribution JAR does not contain built test classes, and we 
need com.metaparadigm.jsonrpc.test.Test in order to run tests. I am using
Eclipse, so I just put the json-rpc project as dependency. The properly
build file will take care of that some day.

License
=======
The code is licenced to you under Apache License 2.0. The license allows use of this software 
for both commercial and non-commercial purposes,
in compiled and source code forms. Please read the license text at:

	 http://www.apache.org/licenses/LICENSE-2.0

Enjoy and let me know of any problems,
Sasha
