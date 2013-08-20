/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.persist;

import com.readyposition.utils.Types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/** XXX - javadoc */
public class SuperFile extends RandomAccessFile
{
    public SuperFile(String name, String mode)
	 throws FileNotFoundException
    {
	super(name, mode);
    }

    public SuperFile(File file, String mode) throws IOException
    {
	super(file, mode);
    }

    private DataInputStream getInput(byte b[]) {
	return new DataInputStream(new ByteArrayInputStream(b));
    }

    public int read(boolean bool[]) throws IOException {
	return read(bool, 0, bool.length);
    }

    public int read(boolean bool[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.BYTE_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    bool[off] = dis.readBoolean();
	return len;
    }

    public int read(char c[]) throws IOException {
	return read(c, 0, c.length);
    }

    public int read(char c[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.CHAR_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    c[off] = dis.readChar();
	return len;
    }

    public int read(short s[]) throws IOException {
	return read(s, 0, s.length);
    }

    public int read(short s[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.SHORT_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    s[off] = dis.readShort();
	return len;
    }

    public int read(int i[]) throws IOException {
	return read(i, 0, i.length);
    }

    public int read(int i[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.INT_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    i[off] = dis.readInt();
	return len;
    }

    public int read(long l[]) throws IOException {
	return read(l, 0, l.length);
    }

    public int read(long l[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.LONG_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    l[off] = dis.readLong();
	return len;
    }

    public int read(float f[]) throws IOException {
	return read(f, 0, f.length);
    }

    public int read(float f[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.FLOAT_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    f[off] = dis.readFloat();
	return len;
    }

    public int read(double d[]) throws IOException {
	return read(d, 0, d.length);
    }

    public int read(double d[], int off, int len) throws IOException {
	// Read the appropriate number of bytes from the file
	byte b[] = new byte[len * Types.DOUBLE_SIZE];
	read(b);
	DataInputStream dis = getInput(b);
	for (int end = off + len; off < end; off++)
	    d[off] = dis.readDouble();
	return len;
    }

    public void write(boolean bool[]) throws IOException {
	write(bool, 0, bool.length);
    }

    public void write(boolean bool[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.BYTE_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeBoolean(bool[off]);
	write(baos.toByteArray());
    }

    public void write(char c[]) throws IOException {
	write(c, 0, c.length);
    }

    public void write(char c[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.CHAR_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeChar(c[off]);
	write(baos.toByteArray());
    }

    public void write(short s[]) throws IOException {
	write(s, 0, s.length);
    }

    public void write(short s[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.SHORT_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeShort(s[off]);
	write(baos.toByteArray());
    }

    public void write(int i[]) throws IOException {
	write(i, 0, i.length);
    }

    public void write(int i[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.INT_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeInt(i[off]);
	write(baos.toByteArray());
    }

    public void write(long l[]) throws IOException {
	write(l, 0, l.length);
    }

    public void write(long l[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.LONG_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeLong(l[off]);
	write(baos.toByteArray());
    }

    public void write(float f[]) throws IOException {
	write(f, 0, f.length);
    }

    public void write(float f[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.FLOAT_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeFloat(f[off]);
	write(baos.toByteArray());
    }

    public void write(double d[]) throws IOException {
	write(d, 0, d.length);
    }

    public void write(double d[], int off, int len) throws IOException {
	ByteArrayOutputStream baos =
	    new ByteArrayOutputStream(len * Types.DOUBLE_SIZE);
	DataOutputStream dos = new DataOutputStream(baos);
	for (int end = off + len; off < end; off++)
	    dos.writeDouble(d[off]);
	write(baos.toByteArray());
    }
}
