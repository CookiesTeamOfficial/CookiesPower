package arkadarktime.interfaces;

public interface ModuleTicker extends ModuleListener {
    void start();

    void stop();

    void update();
}
