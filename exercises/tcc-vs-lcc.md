# TCC *vs* LCC

Explain under which circumstances *Tight Class Cohesion* (TCC) and *Loose Class Cohesion* (LCC) metrics produce the same value for a given Java class. Build an example of such as class and include the code below or find one example in an open-source project from Github and include the link to the class below. Could LCC be lower than TCC for any given class? Explain.

## Answer

TCC = direct pairs / all pairs
LCC = (direct pairs + indirect pairs) / all pairs

### TCC == LCC

If TCC == LCC then
direct pairs == (direct pairs + indirect pairs)
direct pairs == (direct pairs + 0)
indirect pairs must be equal to 0

```java
class Point {
  private double x, y;

  public Point(double x, double y) {
  this.x = x;
  this.y = y;
  }


  public double dot(Point p) {
  return x*p.x + y*p.y;
  }
  public Point sub(Point p) {
  return new Point(x - p.x, y - p.y);
  }
}
```

Direct Nodes : dot & sub => 2
Indirect Nodes : None => 0

TCC = 2/2 
LCC = 2/2

TCC == LCC

###Could LCC be lower than TCC for any given class?

CBT and LCC have the same divisor.
So for LCC to be less than TCC its dividend must be less.

Dividend LCC = (direct even + indirect even)
Dividend TCC = direct even

But **(direct pairs + indirect pairs)** can not be less than **direct pairs**.

So it's impossible