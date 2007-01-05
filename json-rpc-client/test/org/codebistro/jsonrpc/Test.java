/**
 * Based on package com.metaparadigm.jsonrpc.test.Test.java
 */

package org.codebistro.jsonrpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.metaparadigm.jsonrpc.test.Test.Waggle;
import com.metaparadigm.jsonrpc.test.Test.Wiggle;

public interface Test {
	void voidFunction();

	void throwException();

	String[] echo(String strings[]);

	int echo(int i);

	int[] echo(int i[]);

	String echo(String message);

	List echoList(List l);

	byte[] echoByteArray(byte ba[]);

	char[] echoCharArray(char ca[]);

	char echoChar(char c);

	boolean echoBoolean(boolean b);

	boolean[] echoBooleanArray(boolean ba[]);

	Integer[] echoIntegerArray(Integer i[]);

	Integer echoIntegerObject(Integer i);

	Long echoLongObject(Long l);

	Float echoFloatObject(Float f);

	Double echoDoubleObject(Double d);

	Date echoDateObject(Date d);

	Object echoObject(Object o);

	Object echoObjectArray(Object[] o);

	int[] anArray();

	ArrayList anArrayList();

	Vector aVector();

	List aList();

	Set aSet();

	BeanA aBean();

	Hashtable aHashtable();

	String[] twice(String string);

	String concat(String msg1, String msg2);

	Wiggle echo(Wiggle wiggle);

	Waggle echo(Waggle waggle);

	ArrayList aWiggleArrayList(int numWiggles);

	ArrayList aWaggleArrayList(int numWaggles);

	String wigOrWag(ArrayList al);

	// Reference Tests

	static public class RefTest implements Serializable {
		private final static long serialVersionUID = 1;

		private String s;

		public RefTest(String s) {
			this.s = s;
		}

		public String toString() {
			return s;
		}
	}

	static public class CallableRefTest implements Serializable {
		private final static long serialVersionUID = 1;

		private static RefTest ref = new RefTest("a secret");

		public String ping() {
			return "ping pong";
		}

		public RefTest getRef() {
			return ref;
		}

		public String whatsInside(RefTest r) {
			return r.toString();
		}
	}

	CallableRefTest getCallableRef();
}
