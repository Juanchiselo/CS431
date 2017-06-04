package Homework4;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NotifierThread extends Thread
{
    private final Set<ListenerThread> listeners = new CopyOnWriteArraySet<>();

    public NotifierThread(String name)
    {
        super(name);
    }

    public final void addListener(final ListenerThread listener)
    {
        listeners.add(listener);
    }

    public final void removeListener(final ListenerThread listener)
    {
        listeners.remove(listener);
    }

    private final void notifyListeners()
    {
        for (ListenerThread listener : listeners)
            listener.threadEnded(this);
    }

    @Override
    public final void run()
    {
        try {
            doRun();
        } finally {
            notifyListeners();
        }
    }

    public abstract void doRun();
}
