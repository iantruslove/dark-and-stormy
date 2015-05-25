(ns dark-and-stormy.quotes
  (:require [clojure.string :refer [capitalize]]))

(def quotes
  {:hemingway
   ["“There is no friend as loyal as a book.”"
    "“There is nothing to writing. All you do is sit down at a typewriter and bleed.”"
    "“Happiness in intelligent people is the rarest thing I know.”"
    "“I love sleep. My life has the tendency to fall apart when I'm awake, you know?”"
    "“The best way to find out if you can trust somebody is to trust them.”"
    "“Always do sober what you said you'd do drunk. That will teach you to keep your mouth shut.”"
    "“The world breaks everyone, and afterward, many are strong at the broken places.”"
    "“The first draft of anything is shit.”"
    "“All you have to do is write one true sentence. Write the truest sentence that you know.”"
    "“I drink to make other people more interesting.”"
    "“There is nothing noble in being superior to your fellow man; true nobility is being superior to your former self.”"
    "“When people talk, listen completely. Most people never listen.”"
    "“Every day is a new day. It is better to be lucky. But I would rather be exact. Then when luck comes you are ready.”"
    "“It is good to have an end to journey toward; but it is the journey that matters, in the end.”"
    "“The most painful thing is losing yourself in the process of loving someone too much, and forgetting that you are special too.”"
    "“Courage is grace under pressure.”"
    "“Never think that war, no matter how necessary, nor how justified, is not a crime.”"
    "“Never confuse movement with action.”"
    "“The world breaks every one and afterward many are strong at the broken places.”"
    "“But man is not made for defeat\", he said. \"A man can be destroyed but not defeated.”"
    "“As a writer, you should not judge, you should understand.”"
    "“Forget your personal tragedy. We are all bitched from the start and you especially have to be hurt like hell before you can write seriously. But when you get the damned hurt, use it-don't cheat with it.”"
    "“A cat has absolute emotional honesty: human beings, for one reason or another, may hide their feelings, but a cat does not.”"
    "“There's no one thing that's true. It's all true.”"]})

(defn assign-quote [the-quote author]
  (str the-quote " ― " (-> author name capitalize)))

(defn by
  "Returns a quote by `author`."
  [author]
  (apply assign-quote
         (if-let [q (author quotes)]
           [(rand-nth q) author]
           ["“Oops.”" :me])))
