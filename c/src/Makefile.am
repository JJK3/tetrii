CFLAGS=-std=c99 -lm -lpthread

lib_LTLIBRARIES = libtetris.la
libtetris_la_SOURCES = pieces.c pieces.h 

bin_PROGRAMS = tetris
tetris_SOURCES = tetris.c
tetris_CPPFLAGS = @GTK_CFLAGS@
tetris_LDADD = libtetris.la @GTK_LIBS@

CLEANFILES = *~