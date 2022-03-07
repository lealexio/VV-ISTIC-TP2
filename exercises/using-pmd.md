# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false negative). Explain why you would not solve this issue.

## Answer

### Pick a Java project from Github (see the instructions for suggestions). Run PMD on its source code using any ruleset. 

Command : 
```shell script
pmd.bat -d C:/Users/alexl/Documents/FAC/M2/VV/TP2ANNEX/commons-collections-master -R C:/Users/alexl/Documents/FAC/M2/VV/TP2ANNEX/all-java.xml -f summaryhtml > C:/Users/alexl/Documents/FAC/M2/VV/summary.html
```

We use Apache Collections project : https://github.com/apache/commons-collections

### Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. 
PMD detect a lot of errors.
The error is : **Unit tests should not contain more than 1 assert(s)**
```commons-collections-master\src\test\java\org\apache\commons\collections4\AbstractArrayListTest.java```

```java
public void testNewArrayList() {
    final ArrayList<E> list = makeObject();
    assertTrue("New list is empty", list.isEmpty());
    assertEquals("New list has size zero", 0, list.size());

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
}
```

It is better to divide a test containing too many assertions because in case of error, the following assertions will not be tested.


### Describe below an issue found by PMD that is not worth solving (false negative). Explain why you would not solve this issue.

```commons-collections-master\src\main\java\org\apache\commons\collections4\trie\package-info.java```

The error is : **Comment is too large: Too many lines**

A comment that is too long is not a critical problem for the code but rather a problem from the point of view of convention.
