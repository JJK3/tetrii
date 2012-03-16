## Process this file with automake to produce Makefile.in
CFLAGS=-std=c99

TESTS = pieces_test
check_PROGRAMS = pieces_test
pieces_test_SOURCES = pieces_test.c $(top_builddir)/src/pieces.h
pieces_test_CFLAGS = @CHECK_CFLAGS@
pieces_test_LDADD = $(top_builddir)/src/libtetris.la  @CHECK_LIBS@

# 


