
let FileClass = Java.type('java.io.File');

let file = new FileClass("README.md");
let fileName = file.getName();

let JMath = Java.type('java.lang.Math')
let JavaPI = JMath.PI;

print(fileName)
print(JavaPI)

let IntArray = Java.type("int[]");
let iarr = new IntArray(5);

iarr[0] = iarr[iarr.length - 1] * 2;

for (var x in iarr)
  print(x);

let HashMap = Java.type('java.util.HashMap');
let map = new HashMap();
map.put(1, "a");

for (let key in map) {
  print(key + "->" + map.get(key));
}

let ArrayList = Java.type('java.util.ArrayList');
let list = new ArrayList();
list.add(42);
list.add("23");
list.add({});

for (let idx in list) {
  print(idx + "->" + list.get(idx));
}

let JString = Java.type('java.lang.String')
let javaString = new JString("Java");
print(4 === javaString.length);

for (var m in JMath) { if (m.length < 3) print(m); }

try {
  Java.type('java.lang.Class').forName("nonexistent");
} catch (e) {
  print(e.getMessage());
  // e.printStackTrace();
}