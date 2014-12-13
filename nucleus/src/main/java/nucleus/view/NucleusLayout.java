package nucleus.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import nucleus.presenter.Presenter;
import nucleus.presenter.PresenterCreator;

public abstract class NucleusLayout<PresenterType extends Presenter> extends RelativeLayout implements PresenterProvider<PresenterType> {

    private static final String PRESENTER_STATE_KEY = "presenter_state";
    private static final String PARENT_STATE_KEY = "parent_state";

    private PresenterType presenter;
    private Bundle savedPresenterState;

    private Activity activity;

    public NucleusLayout(Context context) {
        super(context);
    }

    public NucleusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NucleusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public PresenterType getPresenter() {
        return presenter;
    }

    protected abstract PresenterCreator<PresenterType> getPresenterCreator();

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        savedPresenterState = bundle.getBundle(PRESENTER_STATE_KEY);
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_STATE_KEY));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode())
            return;

        if (presenter == null) {
            PresenterCreator<PresenterType> creator = getPresenterCreator();
            if (creator != null) {
                presenter = (PresenterType)PresenterFinder.getInstance().findParentPresenter(this).provide(creator, savedPresenterState);
                if (presenter != null)
                    presenter.takeView(this);
            }
        }

        savedPresenterState = null;
        activity = (Activity)getContext();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        if (presenter != null)
            bundle.putBundle(PRESENTER_STATE_KEY, presenter.save());
        bundle.putParcelable(PARENT_STATE_KEY, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (presenter != null) {
            presenter.dropView(this);

            if (activity.isFinishing()) {
                presenter.destroy();
                presenter = null;
            }
        }
    }

    // should be called for a view with a life cycle different to Activity's
    public void destroyPresenter() {
        if (presenter != null) {
            presenter.destroy();
            presenter = null;
        }
    }
}