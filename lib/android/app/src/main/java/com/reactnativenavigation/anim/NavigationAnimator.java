package com.reactnativenavigation.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.reactnativenavigation.parse.AnimationOptions;
import com.reactnativenavigation.parse.FadeAnimation;
import com.reactnativenavigation.parse.NestedAnimationsOptions;
import com.reactnativenavigation.parse.Options;
import com.reactnativenavigation.views.element.ElementTransitionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RestrictTo;

import static com.reactnativenavigation.utils.CollectionUtils.*;

@SuppressWarnings("ResourceType")
public class NavigationAnimator extends BaseAnimator {

    private final ElementTransitionManager transitionManager;
    private Map<View, Animator> runningPushAnimations = new HashMap<>();

    public NavigationAnimator(Context context, ElementTransitionManager transitionManager) {
        super(context);
        this.transitionManager = transitionManager;
    }

    public void push(ViewGroup appearing, ViewGroup disappearing, Options options, Runnable onAnimationEnd) {
        appearing.setAlpha(0);
        AnimatorSet push = options.animations.push.content.getAnimation(appearing, getDefaultPushAnimation(appearing));
        AnimatorSet set = new AnimatorSet();
        List<Animator> elementTransitions = transitionManager.createTransitions(options.animations.transitions, disappearing, appearing);
        if (elementTransitions.isEmpty()) {
            set.playTogether(push);
        } else {
            set.playTogether(merge(new FadeAnimation().content.getAnimation(appearing).getChildAnimations(), elementTransitions));
        }
        set.addListener(new AnimatorListenerAdapter() {
            private boolean isCancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                appearing.setAlpha(1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancelled = true;
                runningPushAnimations.remove(appearing);
                onAnimationEnd.run();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCancelled) {
                    runningPushAnimations.remove(appearing);
                    onAnimationEnd.run();
                }
            }
        });
        runningPushAnimations.put(appearing, set);
        set.start();
    }

    public void pop(View view, NestedAnimationsOptions pop, Runnable onAnimationEnd) {
        if (runningPushAnimations.containsKey(view)) {
            runningPushAnimations.get(view).cancel();
            onAnimationEnd.run();
            return;
        }
        AnimatorSet set = pop.content.getAnimation(view, getDefaultPopAnimation(view));
        set.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                this.cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) onAnimationEnd.run();
            }
        });
        set.start();
    }

    public void setRoot(View root, AnimationOptions setRoot, Runnable onAnimationEnd) {
        root.setVisibility(View.INVISIBLE);
        AnimatorSet set = setRoot.getAnimation(root);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                root.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationEnd.run();
            }
        });
        set.start();
    }

    public void cancelPushAnimations() {
        for (View view : runningPushAnimations.keySet()) {
            runningPushAnimations.get(view).cancel();
            runningPushAnimations.remove(view);
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void endPushAnimation(View view) {
        if (runningPushAnimations.containsKey(view)) {
            runningPushAnimations.get(view).end();
        }
    }
}
