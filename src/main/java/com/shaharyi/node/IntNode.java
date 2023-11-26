public class IntNode {
  private int value;
  private IntNode next;

  public IntNode(int value) {
    this.value = value;
    this.next = null;
  }

  public IntNode(int value, IntNode next) {
    this.value = value;
    this.next = next;
  }

  public int getValue() {
    return value;
  }

  public IntNode getNext() {
    return next;
  }

  public boolean hasNext() {
    return (next != null);
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setNext(IntNode next) {
    this.next = next;
  }

  public String toString() {
    return value + " --> " + next;
  }
}
