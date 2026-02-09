def extracted_method():
  x = 10
  y = 20
  print(x + y)

def foo():
  extracted_method()
  print("hello from foo")

def bar():
  print("hello from bar")
