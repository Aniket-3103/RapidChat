// Generated by view binder compiler. Do not edit!
package com.mishraaniket.project.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.mishraaniket.project.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemReceiveGroupBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView feeling;

  @NonNull
  public final ImageView image;

  @NonNull
  public final LinearLayout linearLayout2;

  @NonNull
  public final TextView message;

  @NonNull
  public final TextView name;

  @NonNull
  public final View view8;

  private ItemReceiveGroupBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView feeling,
      @NonNull ImageView image, @NonNull LinearLayout linearLayout2, @NonNull TextView message,
      @NonNull TextView name, @NonNull View view8) {
    this.rootView = rootView;
    this.feeling = feeling;
    this.image = image;
    this.linearLayout2 = linearLayout2;
    this.message = message;
    this.name = name;
    this.view8 = view8;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemReceiveGroupBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemReceiveGroupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_receive_group, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemReceiveGroupBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.feeling;
      ImageView feeling = ViewBindings.findChildViewById(rootView, id);
      if (feeling == null) {
        break missingId;
      }

      id = R.id.image;
      ImageView image = ViewBindings.findChildViewById(rootView, id);
      if (image == null) {
        break missingId;
      }

      id = R.id.linearLayout2;
      LinearLayout linearLayout2 = ViewBindings.findChildViewById(rootView, id);
      if (linearLayout2 == null) {
        break missingId;
      }

      id = R.id.message;
      TextView message = ViewBindings.findChildViewById(rootView, id);
      if (message == null) {
        break missingId;
      }

      id = R.id.name;
      TextView name = ViewBindings.findChildViewById(rootView, id);
      if (name == null) {
        break missingId;
      }

      id = R.id.view8;
      View view8 = ViewBindings.findChildViewById(rootView, id);
      if (view8 == null) {
        break missingId;
      }

      return new ItemReceiveGroupBinding((ConstraintLayout) rootView, feeling, image, linearLayout2,
          message, name, view8);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}