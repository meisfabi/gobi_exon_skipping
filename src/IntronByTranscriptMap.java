import Model.Intron;

import java.util.*;

public class IntronByTranscriptMap implements Collection<Intron> {
    private HashMap<String, List<Intron>> intronByTranscriptMap= new HashMap<>();
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
        intronByTranscriptMap.putIfAbsent(transcriptId, new ArrayList<>());
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
        return false;
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

    public Set<Map.Entry<String, List<Intron>>> entrySet(){
        return intronByTranscriptMap.entrySet();
    }
}
