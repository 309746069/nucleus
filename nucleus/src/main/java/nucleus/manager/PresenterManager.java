package nucleus.manager;

import android.os.Bundle;
import android.util.Printer;

import nucleus.presenter.Presenter;

/**
 * A singleton that manages presenter's creation and state persistence.
 * This class is intended for internal usage by base classes implementing View part of MVP (NucleusLayout, NucleusActivity)
 * and should not be used by normal View classes.
 */
public abstract class PresenterManager {

    protected PresenterManager() {
    }

    private static PresenterManager instance = new DefaultPresenterManager();

    /**
     * Returns a singleton instance of {@link PresenterManager}
     *
     * @return a singleton instance of {@link PresenterManager}
     */
    public static PresenterManager getInstance() {
        return instance;
    }

    /**
     * This is a testing facility.
     * Use this method to set a custom {@link PresenterManager} instance.
     *
     * @param instance a {@link PresenterManager} instance to set.
     */
    public static void setInstance(PresenterManager instance) {
        PresenterManager.instance = instance;
    }

    /**
     * Finds a Presenter for a given view or restores it from the saved state.
     * There can be three cases when this method is being called:
     * 1. First creation of a view;
     * 2. Restoring of a view when the process has NOT been destroyed (configuration change, activity recreation because of memory limitation);
     * 3. Restoring of a view when the process has been destroyed.
     * <p/>
     * The default implementation searches a passed view for {@link RequiresPresenter} annotation to instantiate a presenter.
     *
     * @return Successively: an overridden, found, restored or created presenter.
     * A RuntimeException will be thrown if no {@link RequiresPresenter} annotation can not be found.
     */
    public abstract <T extends Presenter> T provide(Object view, Bundle savedState);

    /**
     * Creates a bundle that can be used to re-instantiate a presenter. Pass this bundle to {@link #provide}.
     *
     * @param presenter a presenter to obtain restoration bundle for.
     * @return a Bundle that can be used to re-instantiate a presenter.
     */
    public abstract Bundle save(Presenter presenter);

    /**
     * Destroys a presenter, removing all references to it.
     *
     * @param presenter a presenter to destroy.
     */
    public abstract void destroy(Presenter presenter);

    /**
     * Prints a list of presenters and attached views.
     *
     * @param printer a target for printing.
     */
    public abstract void print(Printer printer);
}
