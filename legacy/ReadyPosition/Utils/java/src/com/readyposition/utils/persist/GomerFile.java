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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;

/** XXX - javadoc */
public class GomerFile extends SuperFile
{
    public final static byte BUCKETS = 64;

    class AllocEnum implements Enumeration {
	private GomerFile m_gf;
	private long m_off;

	AllocEnum(GomerFile gf) {
	    m_gf = gf;
	    m_off = 0;
	}

	public boolean hasMoreElements() {
	    Block blk = new Block();
	    while (m_off < m_gf.m_control.m_managed) {
		try {
		    blk.readIn(m_gf, m_off);
		} catch (IOException e) {
		    return false;
		}
		if (blk.m_state == STATE_ALLOCATED)
		    return true;
		m_off += (1 << blk.m_actual);
	    }
	    return false;
	}

	public Object nextElement() {
	    Block blk = new Block();
	    Long result = null;
	    while (result == null && m_off < m_gf.m_control.m_managed) {
		try {
		    blk.readIn(m_gf, m_off);
		    if (blk.m_state == STATE_ALLOCATED)
			result = new Long(m_off + Block.s_size);
		    m_off += (1 << blk.m_actual);
		} catch (IOException e) {
		    return null;
		}
	    }
	    return result;
	}
    }

    public class Stats {
	long m_inUse;
	long m_freeCount[];	// BUCKETS longs
	long m_usedCount[];	// BUCKETS longs

	final static long s_inUse_off = 0;
	final static long s_freeCount_off = s_inUse_off + Types.LONG_SIZE;
	final static long s_usedCount_off = s_freeCount_off +
	    BUCKETS * Types.LONG_SIZE;
	final static long s_size = s_usedCount_off + BUCKETS * Types.LONG_SIZE;

	Stats() {
	    reset();
	}

	void reset() {
	    m_inUse = 0;
	    m_freeCount = new long[BUCKETS];
	    m_usedCount = new long[BUCKETS];
	}

	public long getInUse() { return m_inUse; }
	public long [] getFreeCounts() { return m_freeCount; }
	public long [] getUsedCounts() { return m_usedCount; }

	void readIn(GomerFile gf) throws IOException {
	    m_inUse = gf.readLong();
	    gf.read(m_freeCount);
	    gf.read(m_usedCount);
	}

	void writeOut(GomerFile gf) throws IOException {
	    gf.writeLong(m_inUse);
	    gf.write(m_freeCount);
	    gf.write(m_usedCount);
	}
    }

    class Control {
	byte	m_magic[];	// 4 bytes
	int	m_version;
	long	m_pageSize;
	long	m_managed;
	long	m_label;
	long	m_freeList[];	// BUCKETS longs
	Stats	m_stats;

	final static long s_magic_off = 0L;
	final static long s_version_off = s_magic_off + 4 * Types.BYTE_SIZE;
	final static long s_pageSize_off = s_version_off + Types.INT_SIZE;
	final static long s_managed_off = s_pageSize_off + Types.LONG_SIZE;
	final static long s_label_off = s_managed_off + Types.LONG_SIZE;
	final static long s_freeList_off = s_label_off + Types.LONG_SIZE;
	final static long s_stats_off = s_freeList_off +
	    BUCKETS * Types.LONG_SIZE;
	final static long s_size = s_stats_off + Stats.s_size;

	Control() {
	    m_magic = new byte[4];
	    m_pageSize = 4096;
	    m_freeList = new long[BUCKETS];
	    m_stats = new Stats();
	}

	void readIn(GomerFile gf)
	    throws IOException
	{
	    gf.read(m_magic);
	    m_version = gf.readInt();
	    m_pageSize = gf.readLong();
	    m_managed = gf.readLong();
	    m_label = gf.readLong();
	    gf.read(m_freeList);
	    m_stats.readIn(gf);
	}

	void writeOut(GomerFile gf)
	    throws IOException
	{
	    gf.write(m_magic);
	    gf.writeInt(m_version);
	    gf.writeLong(m_pageSize);
	    gf.writeLong(m_managed);
	    gf.writeLong(m_label);
	    gf.write(m_freeList);
	    m_stats.writeOut(gf);
	}
    }

    class Block {
	byte	m_state;
	byte	m_potential;
	byte	m_actual;
	byte	m_numBuddies;
	long	m_after;

	final static long s_state_off = 0L;
	final static long s_potential_off = s_state_off + Types.BYTE_SIZE;
	final static long s_actual_off = s_potential_off + Types.BYTE_SIZE;
	final static long s_numBuddies_off = s_actual_off + Types.BYTE_SIZE;
	final static long s_after_off = s_numBuddies_off + Types.BYTE_SIZE;
	final static long s_size = s_after_off + Types.LONG_SIZE;

	void readIn(GomerFile gf, long off)
	    throws IOException
	{
	    gf.seek(off);
	    m_state = gf.readByte();
	    m_potential = gf.readByte();
	    m_actual = gf.readByte();
	    m_numBuddies = gf.readByte();
	    m_after = gf.readLong();
	}

	void writeOut(GomerFile gf, long off)
	    throws IOException
	{
	    gf.seek(off);
	    gf.writeByte(m_state);
	    gf.writeByte(m_potential);
	    gf.writeByte(m_actual);
	    gf.writeByte(m_numBuddies);
	    gf.writeLong(m_after);
	}
    }

    final static long PAGESIZE = 4096L;
    final static byte STATE_AVAILABLE = 0;
    final static byte STATE_ALLOCATED = 1;
    final static byte STATE_COALESCED = 2;
    final static byte STATE_SPECIAL = 3;

    private Control m_control = new Control();

    final static byte s_magic[] = {
	(byte)'a', (byte)'l', (byte)'o', (byte)'c'
    };

    public GomerFile(String name, String mode)
	 throws FileNotFoundException, IOException
    {
	super(name, mode);
	attachFile(mode);
    }

    public GomerFile(File file, String mode) throws IOException
    {
	super(file, mode);
	attachFile(mode);
    }

    public int getVersion() { return 1; }

    public byte [] getMagic() {
	return s_magic;
    }

    public void resetFile() throws IOException
    {
	// Truncate the file and then set to initial size
	setLength(0L);
	setLength(PAGESIZE);

	m_control.m_magic = (byte [])getMagic().clone();
	m_control.m_version = getVersion();
	m_control.m_pageSize = PAGESIZE;
	m_control.m_managed = 0L;
	m_control.m_label = -1L;
	Arrays.fill(m_control.m_freeList, -1L);
	m_control.m_stats.reset();

	// The control information is in the first allocated block in
	// the file.  We must skip over the block header to read it.
	seek(Block.s_size);
	m_control.writeOut(this);

	// Manage the original page of the file
	manageNew(0L, PAGESIZE);

	// Allocate space to hold the control info
	seek(allocate(Control.s_size));
	m_control.writeOut(this);

	// Mark the block's state so that it can't be freed
	Block blk = new Block();
	blk.readIn(this, 0L);
	blk.m_state = STATE_SPECIAL;
	blk.writeOut(this, 0L);
    }

    public long allocate(long size) throws IOException {
	// Take size of block into account when allocating
	long fullSize = size + Block.s_size;
	if (size < 0 || fullSize < 0)
	    throw new IOException("Invalid size " + size);

	// What size block do we need?
	byte bucket = 0;
	for (long t = 1; t < fullSize; bucket++)
	    t <<= 1;

	long result = getBlock(bucket, size);
	if (-1L == result) {
	    // We haven't found anything large enough
	    coalesce();

	    if (-1L == (result = getBlock(bucket, size))) {
		moreBytes(bucket);
		return getBlock(bucket, size);
	    }
	}
	return result;
    }

    public long reallocate(long offset, long size) throws IOException {
	if (0 >= offset)
	    return allocate(size);

	// Take size of block into account when allocating
	long fullSize = size + Block.s_size;
	if (size < 0 || fullSize < 0)
	    throw new IOException("Invalid size " + size);

	long blkOff = offset - Block.s_size;
	Block blk = new Block();
	blk.readIn(this, blkOff);
	if (STATE_ALLOCATED != blk.m_state)
	    throw new IOException("Attempt to reallocate() unallocated block");

	long p2 = (1 << blk.m_actual);
	if (p2 >= fullSize) {
	    // There is room to reuse the same block
	    m_control.m_stats.m_inUse += size - blk.m_after;
	    blk.m_after = size;
	    return offset;
	}

	// We need more room
	long newOff = allocate(size);

	// Copy the data to the new location
	byte b[] = new byte[(int)blk.m_after];	// XXX - arrays too short?
	seek(offset);
	read(b);
	seek(newOff);
	write(b);

	free(offset);

	return newOff;
    }

    public void free(long offset) throws IOException {
	// XXX - we should check validity of offset
	offset -= Block.s_size;

	Block blk = new Block();
	blk.readIn(this, offset);
	if (blk.m_state != STATE_ALLOCATED)
	    throw new IOException("Attempt to free unallocated block");

	blk.m_state = STATE_AVAILABLE;
	byte bucket = blk.m_actual;
	m_control.m_stats.m_freeCount[bucket]++;
	m_control.m_stats.m_usedCount[bucket]--;
	m_control.m_stats.m_inUse -= blk.m_after;

	blk.m_after = m_control.m_freeList[bucket];
	m_control.m_freeList[bucket] = offset;

	blk.writeOut(this, offset);
	seek(Block.s_size);
	m_control.writeOut(this);
    }

    public void coalesce() throws IOException {
	Block blk = new Block();
	Block buddy = new Block();

	for (byte i = 0; i < BUCKETS; i++) {
	    byte j = (byte)(i + 1);
	    Block prev = null;
	    long offset = m_control.m_freeList[i];
	    while (-1 != offset) {
		blk.readIn(this, offset);
		long after = blk.m_after;

		// if buddy exists and is free
		// mark the block and its buddy as coalescing
		if (0 != blk.m_numBuddies && STATE_AVAILABLE == blk.m_state) {
		    // This block has a buddy and hasn't been
		    // marked as a buddy of a block earlier in
		    // the free list
		    long buddyOff;
		    if (blk.m_potential != blk.m_actual)
			// buddy is later in the file
			buddyOff = offset + (1 << i);
		    else
			// buddy is earlier in the file
			buddyOff = offset - (1 << i);
		    buddy.readIn(this, buddyOff);

		    if (STATE_AVAILABLE == buddy.m_state &&
			buddy.m_actual == blk.m_actual) {
			// We can coalesce
			buddy.m_state = blk.m_state = STATE_COALESCED;
			m_control.m_stats.m_freeCount[i] -= 2;
			buddy.writeOut(this, buddyOff);
		    }
		}

		if (STATE_COALESCED == blk.m_state) {
		    // Pull block off the free list
		    if (null == prev)
			m_control.m_freeList[i] = blk.m_after;
		    else
			prev.m_after = blk.m_after;

		    if (blk.m_potential != blk.m_actual) {
			// Add to higher free list
			j = ++blk.m_actual;
			blk.m_numBuddies--;
			m_control.m_stats.m_freeCount[j]++;
			blk.m_after = m_control.m_freeList[j];
			m_control.m_freeList[j] = offset;
			blk.m_state = STATE_AVAILABLE;
		    }
		    blk.writeOut(this, offset);
		} else {
		    blk.writeOut(this, offset);
		    prev = blk;
		    blk = new Block();
		}
		offset = after;
	    }
	}
	seek(Block.s_size);
	m_control.writeOut(this);
    }

    public void resize(long pages) throws IOException {
	long high = pages * m_control.m_pageSize;
	setLength(high);

	Block blk = new Block();
	long offset = 0;
	long p2;
	for (long len = high;
	     offset < m_control.m_managed;
	     offset += p2, len -= p2) {
	    blk.readIn(this, offset);
	    p2 = 1 << blk.m_potential;
	    if (len >= (2 * p2)) {
		// Determine largest bucket such that 2**bucket <= length
		byte bucket = 0;
		for (long t = len; t > 1; bucket++)
		    t >>= 1;

		// Make sure blocks have highest potential possible
		promote(blk, offset, bucket);
		blk.writeOut(this, offset);
		break;
	    }
	}

	// The rest of the memory must be new
	manageNew(m_control.m_managed, high);

	seek(Block.s_size);
	m_control.writeOut(this);
    }

    public Enumeration getBlocks() {
	return new AllocEnum(this);
    }

    public long getBlockLength(long offset) throws IOException {
	offset -= Block.s_size;

	Block blk = new Block();
	blk.readIn(this, offset);
	if (blk.m_state != STATE_ALLOCATED)
	    throw new IOException("Attempt to examine unallocated block");

	return blk.m_after;
    }

    public void setLabel(long offset) throws IOException {
	m_control.m_label = offset;
	seek(Block.s_size);
	m_control.writeOut(this);
    }

    public long getLabel() {
	return m_control.m_label;
    }

    public long getOverhead() {
	return Block.s_size;
    }

    public Stats getStats() {
	return m_control.m_stats;
    }

    public long store(byte b)
	 throws IOException
    {
	long off = allocate(Types.BYTE_SIZE);
	seek(off);
	write(b);
	return off;
    }

    public long store(byte b[])
	 throws IOException
    {
	long off = allocate(b.length * Types.BYTE_SIZE);
	seek(off);
	write(b);
	return off;
    }

    public long store(boolean b)
	 throws IOException
    {
	long off = allocate(Types.BOOLEAN_SIZE);
	seek(off);
	writeBoolean(b);
	return off;
    }

    public long store(boolean b[])
	 throws IOException
    {
	long off = allocate(b.length * Types.BOOLEAN_SIZE);
	seek(off);
	write(b);
	return off;
    }

    public long store(char c)
	 throws IOException
    {
	long off = allocate(Types.CHAR_SIZE);
	seek(off);
	writeChar(c);
	return off;
    }

    public long store(char c[])
	 throws IOException
    {
	long off = allocate(c.length * Types.CHAR_SIZE);
	seek(off);
	write(c);
	return off;
    }

    public long store(short s)
	 throws IOException
    {
	long off = allocate(Types.SHORT_SIZE);
	seek(off);
	writeShort(s);
	return off;
    }

    public long store(short s[])
	 throws IOException
    {
	long off = allocate(s.length * Types.SHORT_SIZE);
	seek(off);
	write(s);
	return off;
    }

    public long store(int i)
	 throws IOException
    {
	long off = allocate(Types.INT_SIZE);
	seek(off);
	writeInt(i);
	return off;
    }

    public long store(int i[])
	 throws IOException
    {
	long off = allocate(i.length * Types.INT_SIZE);
	seek(off);
	write(i);
	return off;
    }

    public long store(long l)
	 throws IOException
    {
	long off = allocate(Types.LONG_SIZE);
	seek(off);
	writeLong(l);
	return off;
    }

    public long store(long l[])
	 throws IOException
    {
	long off = allocate(l.length * Types.LONG_SIZE);
	seek(off);
	write(l);
	return off;
    }

    public long store(float f)
	 throws IOException
    {
	long off = allocate(Types.FLOAT_SIZE);
	seek(off);
	writeFloat(f);
	return off;
    }

    public long store(float f[])
	 throws IOException
    {
	long off = allocate(f.length * Types.FLOAT_SIZE);
	seek(off);
	write(f);
	return off;
    }

    public long store(double d)
	 throws IOException
    {
	long off = allocate(Types.DOUBLE_SIZE);
	seek(off);
	writeDouble(d);
	return off;
    }

    public long store(double d[])
	 throws IOException
    {
	long off = allocate(d.length * Types.DOUBLE_SIZE);
	seek(off);
	write(d);
	return off;
    }

    public long store(String s)
	 throws IOException
    {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ObjectOutputStream oos = new ObjectOutputStream(baos);
	oos.writeUTF(s);
	byte b[] = baos.toByteArray();
	long off = allocate(b.length * Types.BYTE_SIZE);
	seek(off);
	write(b);
	return off;
    }

    public long store(Object o)
	 throws IOException
    {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ObjectOutputStream oos = new ObjectOutputStream(baos);
	oos.writeObject(o);
	byte b[] = baos.toByteArray();
	long off = allocate(b.length * Types.BYTE_SIZE);
	seek(off);
	write(b);
	return off;
    }

    void attachFile(String mode) throws IOException
    {
	if (0 == length()) {
	    if (!mode.equals("rw"))
		throw new IOException("Empty file");
	    resetFile();
	    return;
	}

	// The control information is in the first allocated block in
	// the file.  We must skip over the block header to read it.
	seek(Block.s_size);
	m_control.readIn(this);

	if (!Arrays.equals(m_control.m_magic, getMagic()))
	    throw new IOException("Bad magic number");

	if (m_control.m_version != getVersion())
	    throw new IOException("Version mismatch - software(" +
				  getVersion() + "), file(" +
				  m_control.m_version + ")");
    }

    void manageNew(long low, long high) throws IOException {
	long length = high - low;
	if (length < Block.s_size)
	    // If there is some fragment of memory left in a file that
	    // is not a multiple of page size in length we might end
	    // up here.  If (high - low) is 0, we're done and can
	    // terminate the recursion.
	    return;

	// Determine largest bucket such that 2**bucket <= length
	byte bucket = 0;
	for (long t = length; t > 1; bucket++)
	    t >>= 1;

	// Put block of size 2**bucket on the free list
	Block blk = new Block();
	blk.m_potential = bucket;
	blk.m_actual = bucket;
	blk.m_numBuddies = 0;
	blk.m_state = STATE_AVAILABLE;
	blk.m_after = m_control.m_freeList[bucket];
	blk.writeOut(this, low);

	// XXX - only write fields that changed?
	m_control.m_freeList[bucket] = low;
	m_control.m_stats.m_freeCount[bucket]++;
	m_control.m_managed = low + (1 << bucket);

	manageNew(m_control.m_managed, high);
    }

    long getBlock(byte bucket, long size) throws IOException {
	Block blk = new Block();
	Block buddy = new Block();

	long offset = 0;
	byte i, j;

	// Look for an item on a free list equal to or larger than we need
	for (i = bucket; i < BUCKETS; i++)
	    if (-1L != m_control.m_freeList[i]) {
		offset = m_control.m_freeList[i];
		blk.readIn(this, offset);
		m_control.m_freeList[i] = blk.m_after;
		m_control.m_stats.m_freeCount[i]--;
		break;
	    }

	if (BUCKETS == i)
	    // Couldn't find anything big enough
	    return -1L;

	// Split the block if it is larger than we need
	for (j = i; j > bucket; j--) {
	    // byte k = j - (byte)1;
	    byte k = (byte)(j - 1);

	    long buddyOff = offset + (1 << k);
	    buddy.readIn(this, buddyOff);
	    blk.m_numBuddies++;
	    buddy.m_numBuddies = 1;
	    buddy.m_actual = buddy.m_potential = k;
	    buddy.m_state = STATE_AVAILABLE;

	    // Add unused buddy to free list
	    buddy.m_after = m_control.m_freeList[k];
	    buddy.writeOut(this, buddyOff);

	    m_control.m_freeList[k] = buddyOff;
	    m_control.m_stats.m_freeCount[k]++;
	}

	blk.m_state = STATE_ALLOCATED;
	blk.m_actual = bucket;
	blk.m_after = size;
	blk.writeOut(this, offset);

	m_control.m_stats.m_inUse += size;
	m_control.m_stats.m_usedCount[bucket]++;

	seek(Block.s_size);
	m_control.writeOut(this);

	return offset + Block.s_size;
    }

    void moreBytes(byte bucket) throws IOException {
	// We need a block at least as large as p2 = 2**bucket.  This
	// means that the file must grow to a multiple of p2 in length
	// such that at least the last p2 length block is free.
	long contig = 1 << bucket;
	long newLength = (1 + (m_control.m_managed + contig + 1) / contig) *
	    contig;
	long pages = 1 + ((newLength - 1) / m_control.m_pageSize);

	resize(pages);
    }

    void promote(Block blk, long offset, byte bucket) throws IOException {
	Block buddy = new Block();
	for (byte i = blk.m_potential; i < bucket; i++) {
	    long p2 = 1 << i;
	    long buddyOff = offset + p2;
	    buddy.readIn(this, offset + p2);
	    if (buddyOff >= m_control.m_managed) {
		// The buddy is new space
		buddy.m_state = STATE_AVAILABLE;
		buddy.m_potential = buddy.m_actual = i;
		buddy.m_numBuddies = 1;
		buddy.m_after = m_control.m_freeList[i];
		m_control.m_freeList[i] = buddyOff;
		m_control.m_stats.m_freeCount[i]++;
		m_control.m_managed = buddyOff + p2;
	    } else {
		// The buddy is at least partially allocated
		promote(buddy, buddyOff, i);
		buddy.m_numBuddies++;
	    }
	    buddy.writeOut(this, buddyOff);
	    blk.m_numBuddies++;
	}
	blk.m_potential = bucket;
    }

    // XXX - add a JUnit test for this stuff

    public static void main(String args[]) {
	try {
	    GomerFile gf = new GomerFile("foo.aloc", "rw");
	    InputStreamReader reader = new InputStreamReader(System.in);
	    char buf[] = new char[1024];
	    int count;
	    for (System.out.print(">");
		 (count = reader.read(buf)) > 0;
		 System.out.print(">")) {

		String line = new String(buf, 0, count);
		StringTokenizer tok = new StringTokenizer(line);
		String command;

		switch (tok.countTokens()) {
		case 0:
		    break;
		case 1:
		    command = tok.nextToken();
		    if (command.equals("quit"))
			System.exit(0);
		    else if (command.equals("reset")) {
			gf.resetFile();
		    } else if (command.equals("coalesce")) {
			gf.coalesce();
		    } else if (command.equals("stats")) {
			Stats stats = gf.getStats();
			System.out.println("inUse = " + stats.getInUse());
			long free[] = stats.getFreeCounts();
			long used[] = stats.getUsedCounts();
			for (byte i = 0; i < BUCKETS; i++) {
			    long f = free[i];
			    long u = used[i];
			    if (0 != f || 0 != u)
				System.out.println(i + ", " + free[i] +
						   ", " + used[i]);
			}
		    } else if (command.equals("traverse")) {
			Enumeration enum = gf.getBlocks();
			while (enum.hasMoreElements()) {
			    Long l = (Long)enum.nextElement();
			    long off = l.longValue();
			    System.out.println("off = " + off + ", len = " +
					       gf.getBlockLength(off));
			}
		    } else
			usage();
		    break;
		case 2:
		    command = tok.nextToken();
		    if (command.equals("alloc")) {
			long size = Long.parseLong(tok.nextToken());
			System.out.println(gf.allocate(size));
		    } else if (command.equals("free")) {
			long off = Long.parseLong(tok.nextToken());
			gf.free(off);
		    } else if (command.equals("length")) {
			long off = Long.parseLong(tok.nextToken());
			System.out.println(gf.getBlockLength(off));
		    } else
			usage();
		    break;
		case 3:
		    command = tok.nextToken();
		    if (command.equals("realloc")) {
			long off = Long.parseLong(tok.nextToken());
			long size = Long.parseLong(tok.nextToken());
			System.out.println(gf.reallocate(off, size));
		    } else
			usage();
		    break;
		default:
		    usage();
		    break;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static void usage() {
	System.out.println("usage:");
	System.out.println("  alloc size");
	System.out.println("  coalesce");
	System.out.println("  free offset");
	System.out.println("  length offset");
	System.out.println("  realloc offset size");
	System.out.println("  reset");
	System.out.println("  stats");
	System.out.println("  traverse");
	System.out.println("  quit");
    }
}
