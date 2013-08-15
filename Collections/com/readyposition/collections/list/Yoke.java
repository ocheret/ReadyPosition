package com.readyposition.collections.list;

/**
 * The Yoke class implements a doubly linked list. The various
 * algorithms used to manipulate the Yokes in the linked list have some
 * interesting properties that make this implementation more useful
 * than more naive doubly linked list implementations. The algorithms
 * for this implementation originally come from an article from
 * Niklaus Wirth (XXX - can't find the reference).  This class is very
 * powerful but is likely to blow your mind.  It is recommend to work
 * out a few examples by hand to reach an enjoyable epiphany or two.
 *
 * <p>
 * Some of the features include:
 *
 * <ul>
 *
 * <li>Reversible insert operations (e.g. a.insertLeft(b);
 * a.insertLeft(b); done together is a no-op).
 *
 * <li>Commutative insert and append operations (i.e. a.insertLeft(b) and
 * b.insertLeft(a) produce identical results).
 *
 * <li>The ability to splice together multiple doubly linked lists at
 * any point.
 *
 * <li>The ability to divide a doubly linked list in two from any two points in
 * the list.
 *
 * </ul>
 *
 * <p>The following examples should illustrate how insertLeft and
 * insertRight work.
 *
 * <p>If you have the list segment
 *
 * <p><code>...->a->b->c->... (only showing right pointers)</code>
 *
 * <p>and you invoke b.insertLeft(x = new Yoke()) you get...
 *
 * <p><code>...->a-><b>x</b>->b->c->...</code>
 *
 * <p>However, if you invoke b.insertRight(x = new Yoke()) you get...
 *
 * <p><code>...->a->b-><b>x</b>->c->...</code><br>
 *
 * <p>
 * If you have 2 lists (showing circular lists here by repeating the end
 * elements)...
 *
 * <p><code>a->b->c->d->e->a</code><br>
 *
 * <b>p->q->r->s->t->p</b>
 *
 * <p>and you do either a.insertLeft(p) or p.insertLeft(a), you get
 *
 * <p><code><b>p->q->r->s->t</b>->a->b->c->d->e<b>->p</b>
 * </code>
 *
 * <p>Invoking a.insertLeft(p) again, or invoking p.insertLeft(a) splits the
 * long list back into the original 2 lists again.
 *
 * <p>
 * Calling a.insertRight(p) or p.insertRight(a) gives you
 *
 * <p><code>a-><b>q->r->s->t->p</b>->b->c->d->e->a</code>
 *
 * <p>Invoking a.insertRight(p) again, or p.insertRight(a) splits the long lists
 * back into the original 2 lists.  <em>Very Cool!</em>
 *
 * <p> Another way to think of it is that when operating on the two
 * separate lists with a and p, a.insertLeft(p) splices the entire
 * list attached to p to the left of a.  What's more, this splicing is
 * done in such a way that p is the element from its original list
 * that is furthest to the left away from a.
 *
 * <p> When there is just one big list, calling a.insertLeft(p), splits the
 * list in two by separating a from its left neighbor and separating p from its
 * left neighbor to form two smaller circular lists.  Magic.
 *
 * <p> Exercise: What happens if you call a.insertLeft(p) followed by
 * a.insertRight(p)?  Is it useful?
 *
 * <p> I tried unsuccessfully for hours to get this to work properly using
 * Generics.  I came very close but alas no cigar.  For now you need to
 * cast when you subclass from this.
 */
public class Yoke<T extends Yoke<T>> {
    /** The Yoke to the left of this one. */
    @SuppressWarnings("unchecked")
    protected T m_left = (T)this;

    /** The Yoke to the right of this one. */
    @SuppressWarnings("unchecked")
    protected T m_right = (T)this;

    /**
     * Constructor for a Yoke. After construction a Yoke's right and
     * left pointers both refer to the Yoke itself (i.e. a circluar list
     * with one element in it).
     */
    public Yoke() {}

    /**
     * Inserts Yokes into each other's lists. This can be used to
     * splice two lists together. The operation is also
     * reversible. Weird, huh?
     *
     * <p>If you have 2 lists (showing circular lists here by
     * repeating the end elements)...
     *
     * <p><code>a->b->c->d->e->a</code><br>
     *
     * <b>p->q->r->s->t->p</b>
     *
     * <p>and you do either a.insertLeft(p) or p.insertLeft(a), you get
     *
     * <p><code><b>p->q->r->s->t</b>->a->b->c->d->e<b>->p</b>
     * </code>
     *
     * <p>Invoking a.insertLeft(p) again, or invoking p.insertLeft(a)
     * splits the long list back into the original 2 lists again.
     * 
     * @param x the Yoke to insert in our list.  If x is null then nothing
     *          is done.
     */
    @SuppressWarnings("unchecked")
    public void insertLeft(T x) {
        if (x != null) {
            T t = m_left;
            m_left.m_right = x;
            m_left = x.m_left;
            x.m_left.m_right = (T)this;
            x.m_left = t;
        }
    }

    /**
     * Appends Yokes onto each other's lists. This can be used to splice two
     * lists together. The operation is also reversible. Weird, huh?
     *
     * <p>If you have 2 lists (showing circular lists here by
     * repeating the end elements)...
     *
     * <p><code>a->b->c->d->e->a</code><br>
     *
     * <b>p->q->r->s->t->p</b>
     *
     * <p>Calling a.insertRight(p) or p.insertRight(a) gives you
     *
     * <p><code>a-><b>q->r->s->t->p</b>->b->c->d->e->a</code>
     *
     * <p>Invoking a.insertRight(p) again, or p.insertRight(a) splits
     * the long lists back into the original 2 lists.  <em>Very
     * Cool!</em>
     *
     * @param x the Yoke to append to our list.  If x is null then
     *          nothing is done.
     */
    @SuppressWarnings("unchecked")
    public void insertRight(T x) {
        if (x != null) {
            T t = m_right;
            m_right.m_left = x;
            m_right = x.m_right;
            x.m_right.m_left = (T)this;
            x.m_right = t;
        }
    }

    /** Removes a Yoke from a list so it forms its own single element list. */
    @SuppressWarnings("unchecked")
    public void remove() {
        m_left.m_right = m_right;
        m_right.m_left = m_left;
        m_right = m_left = (T)this;
    }

    /** Gets the right Yoke in the list. */
    public T getRight() {
        return m_right;
    }

    /** Gets the left Yoke in the list. */
    public T getLeft() {
        return m_left;
    }
}
