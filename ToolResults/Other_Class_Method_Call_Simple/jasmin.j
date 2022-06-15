.class public Other_Class_Method_Call_Simple
.super java/lang/Object

.method public <init>()V
	.limit stack 1
	.limit locals 1

	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 3
	.limit locals 5

	new List
	dup
	astore_1
	new Other_Class_Method_Call_Simple
	dup
	astore_2
	aload_1
	bipush 10
	invokevirtual List.add(I)I
	istore_3
	iload_3
	istore 4
	aload_2
	aload_1
	invokevirtual Other_Class_Method_Call_Simple.foo(LList;)I
	pop
	return
.end method

.method public foo(LList;)I
	.limit stack 3
	.limit locals 3

	aload_1
	astore_2
	aload_2
	bipush 20
	invokevirtual List.add(I)V
	return
.end method
