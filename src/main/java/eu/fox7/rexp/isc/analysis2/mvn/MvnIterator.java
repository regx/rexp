package eu.fox7.rexp.isc.analysis2.mvn;

import eu.fox7.rexp.isc.analysis2.mvn.MvnRepo.Doc;
import eu.fox7.rexp.util.UtilX;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MvnIterator implements Iterator<MvnRepo.Doc> {
	public static void main(String[] args) {
		MvnIterator it = new MvnIterator(688059403, true);
		for (Doc doc : UtilX.iterate(it)) {
			System.out.println(doc.path);
		}
	}

	private Queue<Doc> queue;
	private final boolean recursive;

	public MvnIterator(int rootId, boolean recursive) {
		queue = new LinkedList<Doc>(get(rootId));
		this.recursive = recursive;
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public Doc next() {
		Doc next = queue.poll();
		if (recursive && !next.isFile()) {
			for (Doc doc : get(next.id)) {
				queue.add(doc);
			}
		}
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported");
	}

	private static Collection<Doc> get(int id) {
		return MvnRepo.get(id).getDocs();
	}
}
