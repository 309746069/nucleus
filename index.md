### The Model-View-Presenter pattern

* **View** is an object that displays data and reacts to user actions.
* **Presenter** is an object that executes commands related background tasks and data changes and provides View with data
* **Model** is a data access object such as database or remote server api

More on this you can find here:
[Wikipedia: Model-View-Presenter](http://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter)

### Inspiration

[Advocating Against Android Fragments](http://corner.squareup.com/2014/10/advocating-against-android-fragments.html)

[mortar](https://github.com/square/mortar)

[Keep It Stupid Simple](http://people.apache.org/~fhanik/kiss.html)

# Problem

* Android development is View-centric
* View can not be considered as a point of stability for application because of it's temporary nature
* Most of modern Android applications just use View-Model architecture
* Programmer is involved into fight with View complexities instead of solving business tasks

### With just Model-View

**You usually end up with**

Everything is connected with everything

![](nucleus-images/everything_is_connected_with_everything.png)

If this diagram does not look complex then think about every arrow is asynchronous and every View can disappear and can be recreated at random time. Add some save/restore of View states, caching of Data and several background threads attached to that temporary Views, and the cake is ready!

**-or with-**

![](nucleus-images/a_god_object.png)

A God object which is overcomplicated, its parts can not be reused, tested or easily refactored.

# Solution

## MVC

![](nucleus-images/mvp.png)

* Complex tasks are split into simpler tasks and are easier to solve
* Less code, less bugs, easier to debug
* Testable

With Nucleus You can use an Activity or a custom Layout to be your View in MVP. Want more? Just create you own View.

## Simplicity

During the development of the Nucleus the primary goal was to create an MVP solution for Android with "Keep It Stupid Simple" in mind. There are about 15Kb of `nucleus.jar` that does all the job.

If you're familiar with the Mortar library then You will find a lot of common. However, Nucleus requires you to write less code. You can use the Dagger for your dependencies, but it is not used for instantiating a Presenter.

One of the shining features of the Nucleus is its Model part of the MVP. While other MVP solutions are, in fact, 'View-Presenter', leaving Presenter without support from the back side.

### Hello world

    public class MainActivity extends NucleusActivity<MainPresenter> {
        @Override
        public MainPresenter createPresenter() {
            return new MainPresenter();
        }
    }

    public class MainPresenter extends Presenter<MainActivity> {
        @Override
        protected void onTakeView(MainActivity view) {
            view.setTitle("Hello, MVP world!");
        }
    }

If You care about your existing Activity class tree - You don't have to use the NucleusActivity class. You can just copy-paste NucleusActivity's code.

### Loader

A simple class that provides a Presenter with data when available. It implements the [Observer](http://en.wikipedia.org/wiki/Observer_pattern) and the [Adapter](http://en.wikipedia.org/wiki/Adapter_pattern) patterns the same time. It adopts different ways of getting data (database, network, cache etc) to fit Nucleus and Android components lifecycle.

    public abstract class Loader<ResultType> {

        public interface Receiver<ResultType> {
            void onLoadComplete(Loader<ResultType> loader, ResultType data);
        }

        public void register(Receiver<ResultType> receiver);
        public void unregister(Receiver<ResultType> receiver);
        protected void notifyReceivers(ResultType data);
    }

A typical Nucleus application subclasses at least one Loader. Subclassing the Loader allows to avoid a boilerplate code in the future. Example: [RetrofitLoader](https://github.com/konmik/nucleus/blob/master/nucleus-example/src/main/java/nucleus/example/network/RetrofitLoader.java).

## Complexity

**A complex task requires a complex solution. Or not?**

Most of the time you just need to pass some data from a Model to a View, making a couple of checks and preparations for the View to make it completely Model-independent. 

You don't want to deal with register/unregister to asynchronous data requests, you don't want to check if a view is exist or it is being recreating right now, you don't want to check if a data from all of required loaders is ready to be presented, etc. All of that are typical tasks. To be short: you don't want to create a boilerplate code. So here is the LoadBroker class that will do all of that for you.

* A **Broker** is used to connect a Presenter with a View.
* A **LoadBroker** is used to connect a Model with a View.

You can think of a Broker as a helping class inside of a Presenter.

### LoadBroker

![](nucleus-images/nucleus_2.png)

* **Loader** - Passes data when available
* **Presenter** - Subscribes to loaders, publishes data when all data from loaders is available and a View is available.
Re-publishes data when the View is being recreated or when new data is available from Loaders.
* **View** - Just shows data

How complex a Presenter's code should be?

    public class MainPresenter extends Presenter<MainActivity> {

        @Override
        public void onCreate(Bundle savedState) {
            addViewBroker(new LoaderBroker<MainActivity>(itemsLoader) {
                @Override
                protected void onPresent(MainActivity view, boolean complete) {
                    view.publishItems(this.getData(itemsLoader));
                }
            });

            itemsLoader.request();
        }
    }

**LoadBroker** reduces a boilerplate code, doing all subscribe/unsubscribe job for you.

![](nucleus-images/broker.png)

## Custom presenter's typical lifecycle

Sometimes it is not enough just to publish a data

    public class MyPresenter extends Presenter<ViewType> {
        @Override
        protected void onCreate(Bundle savedState) {
            // initialize the presenter, start background tasks
        }

        @Override
        protected void onTakeView(ViewType view) {
            // publish some data when a view is attached to the presenter.
        }

        // Use getView() to check availability of a view when receiving a background task result.

        @Override
        protected void onDestroy() {
            // free resources, unsubscribe and cancel background tasks when a user exits a view
        }
    }

# More Features

* **Nested presenters** - you can have your MVP-driven Views to be reused in a different application areas, just override parent presenter's onTakePresenter instead of onTakeView to take control over nested presenter. Or just use Presenter.addPresenterBroker.

* **Smart save/restore** - Presenter gets its view AFTER the view has been completely restored and attached to an Activity. This allows to use View's ability to save and restore its state. It is also extremely useful when dealing with user input such as EditText.

# How to use

* Clone the GitHub [repository](https://github.com/konmik/nucleus)
* Run `mvn clean install` to install it to your local repository
* Maven dependency
```
<dependency>
    <groupId>info.android15.nucleus</groupId>
    <artifactId>nucleus</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```
