package it.marbat.fabtoolbar.lib;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

@CoordinatorLayout.DefaultBehavior(FabToolbar.Behavior.class)
public class FabToolbar extends RevealFrameLayout {

    private static final int DEFAULT_ANIMATION_DURATION = 500;

    private LinearLayout container;
    private FloatingActionButton button;
    private float screenWidth;
    private int animationDuration = DEFAULT_ANIMATION_DURATION;
    private View.OnClickListener clickListener;
    private boolean mVisible;
    private final Rect mShadowPadding = new Rect();

    public FabToolbar(Context context) {
        super(context);
//        this.mInterpolator = new AccelerateDecelerateInterpolator();
        init();
    }

    public FabToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
//        this.mInterpolator = new AccelerateDecelerateInterpolator();
        init();
        loadAttributes(attrs);
    }

    public FabToolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        this.mInterpolator = new AccelerateDecelerateInterpolator();
        init();
        loadAttributes(attrs);
    }

    private void init() {
        this.mVisible = true;
        screenWidth = getResources().getDisplayMetrics().widthPixels;

        inflate(getContext(), R.layout.fab_toolbar, this);
        button = (FloatingActionButton) findViewById(R.id.button);
        Drawable drawable = DrawableCompat.wrap(button.getDrawable());
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), android.R.color.white));
        button.setOnClickListener(new ButtonClickListener());
        container = ((LinearLayout) findViewById(R.id.container));
    }

    private void loadAttributes(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FabToolbar,
                0, 0);

        int containerGravity;
        int buttonGravity;
        try {
            setColor(a.getColor(R.styleable.FabToolbar_tb_color, ContextCompat.getColor(getContext(), R.color.blue)));
            animationDuration = a.getInteger(R.styleable.FabToolbar_tb_anim_duration, DEFAULT_ANIMATION_DURATION);
            containerGravity = a.getInteger(R.styleable.FabToolbar_tb_container_gravity, 1);
            buttonGravity = a.getInteger(R.styleable.FabToolbar_tb_button_gravity, 2);

        }
        finally {
            a.recycle();
        }

        container.setGravity(getGravity(containerGravity));

        FrameLayout.LayoutParams buttonParams = (FrameLayout.LayoutParams) button.getLayoutParams();
        buttonParams.gravity = getGravity(buttonGravity) | Gravity.TOP;
    }

    private int getGravity(int gravityEnum) {
        return (gravityEnum == 0 ? Gravity.START : gravityEnum == 1 ? Gravity.CENTER_HORIZONTAL : Gravity.END)
                | Gravity.CENTER_VERTICAL;
    }

    public void setColor(int color) {
        button.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{color}));
        button.setRippleColor(shiftColorDown(color));
//        button.setColorNormal(color);
//        button.setColorPressed(shiftColorDown(color));
//        button.setColorRipple(shiftColorDown(color));
        container.setBackgroundColor(color);
    }

    public void setAnimationDuration(int duration) {
        animationDuration = duration;
    }

    public void setButtonOnClickListener(View.OnClickListener listener) {
        clickListener = listener;
    }

//    public void attachToListView(AbsListView listView) {
//        button.attachToListView(listView);
//    }
//
//    public void attachToRecyclerView(RecyclerView recyclerView) {
//        button.attachToRecyclerView(recyclerView);
//    }

    public void setButtonIcon(Drawable drawable) {
        button.setImageDrawable(drawable);
    }

    public void setButtonIcon(int resId) {
        button.setImageResource(resId);
    }

    public void show() {
        button.setOnClickListener(null);
        container.setVisibility(VISIBLE);
        animateCircle(0, screenWidth, null);
    }

    public void hide() {
        //If button was attached to list and got hidden, closing the toolbar should still show the button
//        button.show(false);
        button.show();
        animateCircle(screenWidth, 0, new ToolbarCollapseListener());
    }

    public boolean isVisible() {
        return (container.getVisibility() == VISIBLE);
    }

    private void animateCircle(float startRadius, float endRadius, SupportAnimator.AnimatorListener listener) {
        int cx = (button.getLeft() + button.getRight()) / 2;
        int cy = (button.getTop() + button.getBottom()) / 2;

        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(container, cx, cy, startRadius, endRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(animationDuration);
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }

    public boolean isShowing() {
        return this.mVisible;
    }

    public void scrollUp() {
//        this.scrollUp(true);
        mVisible = true;
        if (isVisible())
            hide();
        button.show();
    }

    public void scrollDown() {
        mVisible = false;
//        this.scrollDown(true);
        if (isVisible())
            hide();
        button.hide();
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams)layoutParams).bottomMargin;
        }

        return marginBottom;
    }

    @Override
    public void addView(@NonNull View child) {
        if (canAddViewToContainer(child)) {
            container.addView(child);
        }
        else {
            super.addView(child);
        }
    }

    @Override
    public void addView(@NonNull View child, int width, int height) {
        if (canAddViewToContainer(child)) {
            container.addView(child, width, height);
        }
        else {
            super.addView(child, width, height);
        }
    }

    @Override
    public void addView(@NonNull View child, ViewGroup.LayoutParams params) {
        if (canAddViewToContainer(child)) {
            container.addView(child, params);
        }
        else {
            super.addView(child, params);
        }
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (canAddViewToContainer(child)) {
            container.addView(child, index, params);
        }
        else {
            super.addView(child, index, params);
        }
    }

    private boolean canAddViewToContainer(View child) {
        return container != null && !(child instanceof FloatingActionButton);
    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            show();

            if (clickListener != null) {
                clickListener.onClick(v);
            }
        }
    }

    private class ToolbarCollapseListener implements SupportAnimator.AnimatorListener {
        @Override
        public void onAnimationEnd() {
            container.setVisibility(GONE);
            button.setOnClickListener(new ButtonClickListener());
        }

        @Override public void onAnimationStart() {}

        @Override public void onAnimationCancel() {}

        @Override public void onAnimationRepeat() {}
    }

    public static class Behavior extends android.support.design.widget.CoordinatorLayout.Behavior<FabToolbar> {
        //        private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
        private Rect mTmpRect;
//        private float mTranslationY;

        private ValueAnimatorCompat mFabTranslationYAnimator;
        private float mFabTranslationY;

        public Behavior() {
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, FabToolbar child, View dependency) {
            return (SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout)
                    || dependency instanceof AppBarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FabToolbar child, View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                updateFabTranslationForSnackbar(parent, child, dependency);
            } else if (dependency instanceof AppBarLayout) {
                // If we're depending on an AppBarLayout we will show/hide it automatically
                // if the FAB is anchored to the AppBarLayout
                updateFabVisibility(parent, (AppBarLayout) dependency, child);
            }
            return false;
        }

        @Override
        public void onDependentViewRemoved(CoordinatorLayout parent, FabToolbar child, View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                updateFabTranslationForSnackbar(parent, child, dependency);
            }
        }

        private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, FabToolbar child) {
//            final CoordinatorLayout.LayoutParams lp =
//                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
//            if (lp.getAnchorId() != appBarLayout.getId()) {
//                // The anchor ID doesn't match the dependency, so we won't automatically
//                // show/hide the FAB
//                return false;
//            }

            if (child.getVisibility() == INVISIBLE || child.getVisibility() == GONE)
                // The view isn't set to be visible so skip changing it's visibility
                return false;

            if(this.mTmpRect == null) {
                this.mTmpRect = new Rect();
            }
            int rect_bottom = this.mTmpRect.bottom;
//                Log.i(getClass().toString(), "this.mTmpRect prima: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom prima: " + rect_bottom);
            ViewGroupUtils.getDescendantRect(parent, appBarLayout, this.mTmpRect);
//                Log.i(getClass().toString(), "this.mTmpRect dopo: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom dopo: " + rect_bottom);
            if(rect_bottom > mTmpRect.bottom) {
                if(child.isShowing())
                    child.scrollDown();
            }
            else {
                if (!child.isShowing())
                    child.scrollUp();
            }
            return true;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent,
                                                     final FabToolbar fab, View snackbar) {
            final float targetTransY = getFabTranslationYForSnackbar(parent, fab);
            if (mFabTranslationY == targetTransY) {
                // We're already at (or currently animating to) the target value, return...
                return;
            }

            final float currentTransY = ViewCompat.getTranslationY(fab);

            // Make sure that any current animation is cancelled
            if (mFabTranslationYAnimator != null && mFabTranslationYAnimator.isRunning()) {
                mFabTranslationYAnimator.cancel();
            }

            if (fab.isShown()
                    && Math.abs(currentTransY - targetTransY) > (fab.getHeight() * 0.667f)) {
                // If the FAB will be travelling by more than 2/3 of it's height, let's animate
                // it instead
                if (mFabTranslationYAnimator == null) {
                    mFabTranslationYAnimator = ViewUtils.createAnimator();
                    mFabTranslationYAnimator.setInterpolator(
                            AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    mFabTranslationYAnimator.setUpdateListener(
                            new ValueAnimatorCompat.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimatorCompat animator) {
                                    ViewCompat.setTranslationY(fab,
                                            animator.getAnimatedFloatValue());
                                }
                            });
                }
                mFabTranslationYAnimator.setFloatValues(currentTransY, targetTransY);
                mFabTranslationYAnimator.start();
            } else {
                // Now update the translation Y
                ViewCompat.setTranslationY(fab, targetTransY);
            }

            mFabTranslationY = targetTransY;
        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent,
                                                    FabToolbar fab) {
            float minOffset = 0;
            final List<View> dependencies = parent.getDependencies(fab);
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                final View view = dependencies.get(i);
                if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(minOffset,
                            ViewCompat.getTranslationY(view) - view.getHeight());
                }
            }

            return minOffset;
        }

//        @Override
//        public boolean onLayoutChild(CoordinatorLayout parent, FabToolbar child, int layoutDirection) {
//            // First, lets make sure that the visibility of the FAB is consistent
//            final List<View> dependencies = parent.getDependencies(child);
//            for (int i = 0, count = dependencies.size(); i < count; i++) {
//                final View dependency = dependencies.get(i);
//                if (dependency instanceof AppBarLayout
//                        && updateFabVisibility(parent, (AppBarLayout) dependency, child)) {
//                    break;
//                }
//            }
//            // Now let the CoordinatorLayout lay out the FAB
//            parent.onLayoutChild(child, layoutDirection);
//            // Now offset it if needed
//            offsetIfNeeded(parent, child);
//            return true;
//        }
//
//
//        /**
//         * Pre-Lollipop we use padding so that the shadow has enough space to be drawn. This method
//         * offsets our layout position so that we're positioned correctly if we're on one of
//         * our parent's edges.
//         */
//        private void offsetIfNeeded(CoordinatorLayout parent, FabToolbar fab) {
//            final Rect padding = fab.mShadowPadding;
//
//            if (padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
//                final CoordinatorLayout.LayoutParams lp =
//                        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//
//                int offsetTB = 0, offsetLR = 0;
//
//                if (fab.getRight() >= parent.getWidth() - lp.rightMargin) {
//                    // If we're on the left edge, shift it the right
//                    offsetLR = padding.right;
//                } else if (fab.getLeft() <= lp.leftMargin) {
//                    // If we're on the left edge, shift it the left
//                    offsetLR = -padding.left;
//                }
//                if (fab.getBottom() >= parent.getBottom() - lp.bottomMargin) {
//                    // If we're on the bottom edge, shift it down
//                    offsetTB = padding.bottom;
//                } else if (fab.getTop() <= lp.topMargin) {
//                    // If we're on the top edge, shift it up
//                    offsetTB = -padding.top;
//                }
//
//                fab.offsetTopAndBottom(offsetTB);
//                fab.offsetLeftAndRight(offsetLR);
//            }
//        }
    }

    private static int shiftColorDown(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }
}
