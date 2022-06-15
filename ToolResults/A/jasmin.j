.class public A
.super B

.method public <init>()V
	.limit stack 1
	.limit locals 1

	aload_0
	invokespecial B/<init>()V
	return
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 0
	.limit locals 1

	return
.end method

.method public foo()LA;
	.limit stack 1
	.limit locals 3

	new A
	dup
	astore_1
	new B
	dup
	astore_2
	aload_1
	astore_2
	aload_1
	areturn
.end method
