
# Syntax Guide (suggestions)

Below are suggestions for our choice of syntax throughout the application.

Add/modify this document to update the guide.

```java
public class Example {
	private boolean noLineBreak;	// 1) No empty line break from class declaration to first field/method (Main class is exception)

	// 2) Use camel cased notation for fields and methods
	private boolean camelCased;

	// 3) Number of spaces per tab: 4
	public void spacesPerTab() {
		// this is indented 4 spaces (1 tab)
	}

	public void explicitType() {
		// 4) Use explicit type instead of "var" even when obvious
		int number1 = 5;

		// ...instead of "var"
		var number2 = 5;
	}

	public void noCurlyBraces(boolean arg) {
		// 5a) Don't use curly braces when unnecessary
		if (arg)
			doSomething();

		// 5b) Still unecessary
		if (arg)
			for (int i = 0; i < 10; i++)
				doSomething();
	}

	public void secondaryStatementsOnNewLine(int arg) {
		// 6a) Start secondary statements on new lines rather than on same line
		if (arg == 0) {
			doSomething();
			doSomethingMore();
		}
		else if (arg > 0) { // <-- starting on new line
			// ..
			// ..
		}

		// 6b)
		try {
			doSomething();
		}
		catch(Exception e) {	// <-- starting on new line
			doSomethingElse();
		}
	}


	public void nameOfException(int arg) {
		// 7) Call the exception object "e" rather than "ex" or "err"
		try {
			// ..
		}
		catch(Exception e) {
			// ..
		}
	}

	/**
	* Description of what the method does. (using imperative form, e.g. "Add a user" instead of "Adds.." or "Will add.."
	*
	* @param arg - Describe the argument.
	*/
	public void documentPublicMethods(boolean arg) {
		// 8) Use JavaDocs for public methods (no need for the private ones)
	}
}
```

