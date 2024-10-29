import Model.Intron;

import java.util.*;

public class IntronByTranscriptMap implements Collection<Intron> {
    private final HashMap<String, TreeSet<Intron>> intronByTranscriptMap = new HashMap<>();

    @Override
    public int size() {
        return intronByTranscriptMap.size();
    }

    @Override
    public boolean isEmpty() {
        return intronByTranscriptMap.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return intronByTranscriptMap.containsKey(o.toString());
    }

    @Override
    public Iterator<Intron> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return intronByTranscriptMap.entrySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Intron intron) {
        var transcriptId = intron.getTranscriptId();
        intronByTranscriptMap.putIfAbsent(transcriptId, new TreeSet<>());
        intronByTranscriptMap.get(transcriptId).add(intron);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        intronByTranscriptMap.remove(o);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Intron> c) {
        String transcriptId = null;
        for (var intron : c) {
            transcriptId = intron.getTranscriptId();
            if (transcriptId != null)
                break;
        }
        if (transcriptId == null) return false;
        intronByTranscriptMap.putIfAbsent(transcriptId, new TreeSet<>(c));
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        intronByTranscriptMap.clear();
    }

    public Set<Map.Entry<String, TreeSet<Intron>>> entrySet() {
        return intronByTranscriptMap.entrySet();
    }

    public TreeSet<Intron> get(String key) {
        return intronByTranscriptMap.get(key);
    }
}
