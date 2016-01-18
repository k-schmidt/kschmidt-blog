# Structure and Interpretation of Computer Programs - Chapter 1.1
## Introduction by Alan J. Perlis
* A programmer should acquire good algorithms and idioms.
* It is better to have 100 functions operate on one data structure than to have 10 functions operate on 10 data structures.
* Programs must be written for people to read and only incidentally for machines to execute.
* John McCarthy - founder of Lisp in the 1950s and wrote "Recursive Functions of Symbolic Expressions and Their Computation by Machine".

## 1.1.0 The Elements of Programming
All programming languages must have:
* Primitive Expressions
 * Represent the simplest entities the language is concerned with.
* Means of Combination
 * By which compound elements are built from simpler ones.
* Means of Abstraction
 * By which elements can be named and manipulated as units.

Function's prefix notation has several advantages:
* Procedures can be an arbitrary number of elements

```clojure
  (+ 1 2 3 4 5 24)
```

* Allows combinations to be nested and to have combinations whose elements themselves are combinations.

```clojure
(+ (+ 1 2) (+ 3 4) (+ 5 6))
```

## 1.1.2 Naming and the Environment
* We say that the name identifies a variable whose value is the object.
* *def* is our language's simplest means of abstraction, for it allows us to use simple names to refer to the results of compound operations.

```clojure
(def radius 10)

(def pi 3.14159)

(def circumference
  (* 2 pi radius))
```

## 1.1.3 Evaluating Combinations
* Our goal is to isolate our issues about thinking procedurally.
 * The objective is to think in combinations.

To evaluate a combination:
1. Evaluate the subexpressions of the combination.
2. Apply the procedure that is the value of the leftmost subexpression (the operator) to the arguments that are the values of the other subexpressions (the operands).

Thereby we must first evaluate all inner expressions **first** before we can evaluate the entire expression.
We must evaluate each element of the combination. Thus, the evaluation rule is recursive in nature.
 * It includes, as one of its steps, the need to invoke the rule itself.

## 1.1.4 Compound Procedures
* Numbers and arithmetic operations are primitive data and procedures.
* Nesting of combinations provides a means of combining operations.
* Definitions that associate names with values provide a limited means of abstraction.

*Procedure Definitions* - compound operations can be given a name and then referred to as a unit.

*General Form of Procedural Definition*

`(defn <name> [<formal parameters>]
  <body>)`

`<name>` is a symbol to be associated with the procedure definition in the environment.

`<formal parameters>` are names used within the body of the procedure to refer to the corresponding arguments of the procedure.

`<body>` an expression that will yield the value of the procedure application when the formal parameters are replaced by the actual arguments to which the procedure is applied.

## 1.1.5 The Substitution Model for Procedure Application
* To apply a compound procedure to arguments, evaluate the body of the procedure with each formal parameter replaced by the corresponding argument.

Evaluate `(f 5)`
We define f to be:

```clojure
(defn square [x]
  (* x x))

(defn sum-of-squares [x y]
  (+ (square x) (square y)))

(defn f [a]
  (sum-of-squares (+ a 1) (* a 2)))

(sum-of-squares (+ 5 1) (* 5 2))

(+ (square 6) (square 10))

(+ (* 6 6) (* 10 10))

(+ 36 100)

136
```

* *Substitution Model* - model that determines the meaning of procedure applications.
 * Just a model to help us think about procedure applications, not to provide a description of how the interpreter really works.
* When modeling phenomena in science and engineering, we begin with simplified, incomplete models.
 * Simple models are transitioned to more refined ones.

Applicative Order Versus Normal Order
Inside-Out Versus Outside-In
* Normal order model is the evaluation method of fully expanding a procedure's body and then reducing to the answer.
 * First trace the program and expand all arguments and procedures to their fullest form.
* **However** the interpreter uses an *evaluate the arguments and then apply* approach called *Applicated-order Evaluation*.
* Lisp uses applicative-order evaluation because of efficiency of not duplicating expressions and also normal-order evaluation becomes much more complicated when we leave the realm of substitution.

## 1.1.6 Conditional Expressions and Predicates
Case analysis through `cond`

```clojure
(defn abs [x]
  (cond ((> x 0) x)
        ((= x 0) 0)
        ((< x 0) (- x))))
```

General Form

`(cond (<p1> <e1>)
       (<p2> <e2>) ...
       (<pn> <en>))`

Symbol cond followed by parenthesized pairs of expressions `(<p> <e>)` called clauses where `<p>` value is either True or False.
Predicates `<p1>...<pn>` are checked for 'truthiness' and takes the first truthy result that the procedure finds (consequent expression `<e>`). If none of the `<p>'s` are true then the value of cond is undefined.

Another way of computing the absolute value:

```clojure
(defn abs [x]
  (cond ((< x 0) (- x))
        (else x)))
```

*Else* is a special symbol that can be used in place of the `<p>` in the final clause of cond.

Yet another way...:

```clojure
(defn abs [x]
  (if (< x 0) (- x) x))
```

* Uses special form *if*, a restricted type of conditional that is used when there are precisely two cases in the case analysis.

General Form

`(if <predicate> <consequent> <alternative>)`

* The interpreter evaluates the `<predicate>`
 * If true, it evaluates the `<consequent>`
 * If false, it returns the evaluation of the `<alternative>`

* Clojure has primitive predicates <, >, =
 * Also logical composition operations to support construction of compound predicates.

General Form

`(and <e1> <e2> ... <en>)`

* From left to right, if any `<e>` evaluates to false then the value of the and expression is false and the remaining `<e>`'s are not evaluated.
 * If all <e>'s are true then the and expression is the value of the last `<e>`.

General Form

`(or <e1> <e2> ... <en>)`

* If any <e> is true, that value is returned as the value of the or expression and the remainder are unevaluated.
 * If all <e>'s evaluate to false then the value of the or expression is false.

General Form

`(not <e>)`

* Value of not expression is true when <e> is false and vice versa.

* and, or are special forms
 * The subexpression are not necessarily all evaluated.
* not is an ordinary procedure.

Ex 1.3
```clojure
(defn sum-max [a b c]
  (cond ((and (> a c) (> b c)) (+ a b))
        ((and (> b a) (> c a)) (+ b c))
        ((and (> a b) (> c b)) (+ a c))))
```

Ex 1.4
To apply a compound procedure to arguments, evaluate the body of the procedure with each formal parameter replaced by the corresponding argument.
```clojure
(defn a-plus-abs-b [5 -3]
  ((if (> -3 0) + -) 5 -3))

(- 5 -3)
8
```

Ex 1.5
I believe Ben will receive and error in normal-order evaluation because the function (p) does not resolve and normal-order evaluation must blow-out all operations before evaluation. In applicative-order evaluation we replace the parameters of the body with the corresponding arguments and evaluate them if need be. The if special form never evaluates y because its predicate is true, resulting in the value 0.

## 1.1.7 Example: Square Roots by Newton's Method
* Procedure must be effective
 * Mathematical functions are not procedures because they do not describe a procedure.
* Function vs. Procedure
 * Distinction between describing properties of things and describing how to do things.
  * Declarative knowledge vs. imperative knowledge
  * What is vs. How to
* Newton's method of successive approximations.
We have a guess y for the value of a square root of a number x. We can perform a simple manipulation to get a better guess by averaging y with x/y.

| Guess | Quotient | Average |
| ----- | -------- | ------- |
| 1 | 2/1 = 2 | ((2 + 1)/2) = 1.5 |
| 1.5 | 2/1.5 = 1.3333 | ((1.3333 + 1.5)/2) = 1.4167 |
| 1.4167 | (2/1.4167) = 1.4118 | ((1.4167 + 1.4118)/2) = 1.4142 |
| 1.4142... |  |  |  |

Simple Model of Procedure:
```clojure
(defn sqrt-iter [guess x]
  (if (good-enough? guess x)
      guess
      (sqrt-iter (improve guess x)
      x)))

(defn improve [guess x]
  (average guess (/ x guess)))

(defn (average x y)
  (/ (+ x y) 2))
```

What is good enough? Choose! 0.001

```clojure
(defn good-enough? [guess x]
  (< (abs (- (square guess) x)) .001))
```

Finally, we need an initial guess, 1.0
```clojure
(defn sqrt [x]
  (sqrt-iter 1.0 x))
```

Ex 1.6
```clojure
(defn new-if [predicate then-clause else-clause]
  (cond (predicate then-clause)
        (else else-clause)))
        
(defn sqrt-iter [guess x]
  (new-if (good-enough? guess x)
          guess
          (sqrt-iter (improve guess x)
                      x)))
```
Both conditions in a `cond` are evaluated versus the `if` statement which first evaluates the `predicate` and then either the `consequent` or the `alternative`.

Ex 1.7
`(sqrt .0001)` yields .0323 instead of the expected .01, yielding an error of 200%. For very large numbers, the machine is not able to represent very small differences. For very large numbers, .001 is too generous.
```clojure
(defn sqrt-iter [guess old-guess x]
  (if (good-enough? guess old-guess)
        guess
      (sqrt-iter (improve guess x) guess x)))

(defn good-enough? [guess old-guess]
  (< (abs (- guess old-guess))
     (* .001 guess)))
     
(defn sqrt [x]
  (sqrt-iter 1.0 2.0 x))
```

Ex 1.8
```clojure
(defn cube [x]
  (* x x x))
  
(defn improve [guess x]
  (/ (+ (* 2 guess) (/ x (square guess)))
     (3)))

(defn good-enough? [guess x]
  (< (abs (- (cube guess) x)) .001))
  
(defn cube-root-iter [guess x]
  (if (good-enough? guess x)
        guess
      (cube-root-iter (improve guess x) x)))

(defn cube-root [x]
  (cube-root-iter 1.0 x))
```

## 1.1.8 Procedures as Black-Box Abstractions
* In the square-root and cube-root problems we observed that the computation breaks up naturally into a number of *subproblems*.
  * Each accomplished by a separate procedure
  * The sqrt unit is a cluster of procedures.
* It is crucial that each procedure accomplishes an identifiable task that can be used as a module in defining other procedures.
* The higher order functions or *procedural abstractions* abstract away the "how" of the program and we only concern ourselves with the fact that the unit computes and gives an expected result.
* Procedural definitions/abstractions suppress detail.
  * A user of the procedure should not need to know how the procedure is implemented in order to use it.

Local Names
* The name of the formal parameter of a procedure doesn't matter.
* Such a name is called a *bound variable*, and we say that the procedure *binds* its formal parameters.

