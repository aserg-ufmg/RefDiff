def extracted_method
  x = 10
  y = 20
  puts x + y
end

def foo
  extracted_method
  puts "hello from foo"
end

def bar
  puts "hello from bar"
end
