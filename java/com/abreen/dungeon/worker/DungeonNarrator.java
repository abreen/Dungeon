package com.abreen.dungeon.worker;

import java.util.*;
import com.abreen.dungeon.model.*;

/*
 * Provides static methods that provide varied and interesting English
 * narration based on actions performed in the universe.
 */
public class DungeonNarrator {
  private static final long DEFAULT_RANDOM_SEED = 6;

  private Random r;

  public DungeonNarrator() {
    this.r = new Random();
  }

  public static void main(String[] args) {
    DungeonNarrator n = new DungeonNarrator();
    System.out.println(n.narrateMoveToRoom("James", "the hallway"));
    System.out.println(n.narrateMoveInDirection("Sana", "northeast"));
    System.out.println(n.narrateTake("Jeffery", "the key"));
    System.out.println(n.narrateDrop("Karl", "the notebook"));
    System.out.println(n.narrateGive("Moritz", "the pen", "Georg"));
    System.out.println(n.narrateSay("Jason", "I like pancakes."));
    System.out.println(n.narrateSay(
      "Marta", "I'd like some water, because I'm thirsty."));
    System.out.println(n.narrateSay(
      "Chris", "Either we leave now, or we leave in a couple minutes."));
    System.out.println(n.narrateSay(
      "Tomas", "If I were to stay behind, would you guys wait for me?"));
    System.out.println(n.narrateSay(
      "Tomas", "Would you guys wait for me if I were to stay behind?"));
    System.out.println(n.narrateSay("Alex", ""));
    System.out.println(n.narrateYell("Rachel", "Help me!"));
    System.out.println(n.narrateDistantYell("This is outrageous!"));
  }
  
  public static enum StringType {
    WITH_ARTICLE, WITH_DEFINITE_ARTICLE, WITH_INDEFINITE_ARTICLE,
    WITHOUT_ARTICLE;
  }
  
  /**
   * Given a describable ingame object, the method will return a string
   * containing the ingame object or item's description.
   * 
   * This method determines the type of the object and uses the appropriate
   * methods to construct a description, for ease of modifiability and
   * internationalization.
   * 
   * @see Describable
   * @see StringType
   * @param d The describable object
   * @return A string containing the object's description
   */
  public static String describe(Describable d) {
    return d.getDescription();
  }
  
  /**
   * Given a player and a room object, this method will return a string
   * containing a list of players in the room from the specified player's
   * perspective (i.e., the player specified here will be mentioned as "you"
   * in the list).
   * 
   * @param perspective The player to list as "you"
   * @param r The room to search for players
   * @return A string listing the players in the room, or null if there are none
   */
  public static String describePlayers(Player perspective, Room r) {
    String str = "";
    
    int size;
    if ((size = r.getNumberOfPlayers()) > 0) {
      
      if (size == 1)
        str += "Player ";
      else
        str += "Players ";
      
      Iterator<Player> ps = r.getPlayers();
      
      int i = 1;
      while (ps.hasNext()) {
        Player p = ps.next();
        
        if (p == perspective)
          str += toString(p) + " (you)";
        else
          str += toString(p);
        
        if (i == size - 1) {
          if (size == 2) {
            str += " and ";
          } else {
            str += ", and ";
          }
        } else if (i != size) {
          str += ", ";
        }
        
        i++;
      }
      
      if (size == 1)
        str += " is here.";
      else
        str += " are here.";
    } else {
      return null;  // if there are no players here
    }

    return str;
  }
  
  /**
   * Given a room object, this method will return a string containing a list
   * of items currently in the room.
   * 
   * @param r The room to search for items
   * @return A string listing the items in the room, or null if there are none
   */
  public static String describeItems(Room r) {
    if (r.hasNoItems())
      return null;
    
    return toNaturalList(r.getItems());
  }
  
  /**
   * Given a describable object and a StringType constant, convert the ingame
   * object to a string with the specified qualities.
   * 
   * This method uses the appropriate methods of the object itself for
   * ease of modifiability and internationalization.
   * 
   * @see Describable
   * @see StringType
   * @param d The describable object
   * @param t Constant referring to the type of output
   * @return A string representation of the object
   */
  public static String toString(Describable d, StringType t) {
    String str = d.getName();
    
    if (d.neverUseArticle())
      return str;
    
    switch (t) {
      case WITH_ARTICLE:
      case WITH_DEFINITE_ARTICLE:
        return "the " + str;
      case WITH_INDEFINITE_ARTICLE:
        if (d.startsWithVowel())
          return "an " + str;
        else
          return "a " + str;
      case WITHOUT_ARTICLE:
      default:
        return str;
    }
  }
  
  public static String toString(Player p) {
    return toString(p, StringType.WITHOUT_ARTICLE);
  }
  
  /**
   * Returns the specified string with its first character as a capital letter.
   * @param s The string
   * @return The string, with a capitalized first character
   */
  public static String capitalize(String s) {
    return s.substring(0, 1).toUpperCase() +
           s.substring(1);
  }
  
  /**
   * Returns a String representation of a list of objects, in narrative
   * style. Fully generalized to accept an array of any object type and
   * uses the toString() method.
   * @param a The array of objects
   * @return The array's string representation
   */
  public static String toNaturalList(Object[] a) {
    int size = a.length;
    String str = "";
    
    if (size == 0)
      return "Nothing.";
    
    str += capitalize(a[0].toString());
    
    if (size == 1)
      return str + ".";
    else
      str += ", ";
    
    for (int i = 1; i < a.length; i++) {
      
      str += a[i].toString();
      
      if (!((i + 1) < a.length)) {
        str += ".";
        break;
      } else {
        str += ", ";
      }
    }
    
    return str;
  }
  
  /**
   * Returns a string representation of a collection of items, in narrative style.
   * @param a The collection of items
   * @return The collection's string representation
   */
  public static String toNaturalList(Collection<Item> a) {
    int size = a.size();
    String str = "";
    
    if (size == 0)
      return "No items.";
    
    Iterator<Item> iter = a.iterator();
    Item i = iter.next();
    
    str += capitalize(toString(i, StringType.WITHOUT_ARTICLE));
    
    if (size == 1)
      return str + ".";
    else
      str += ", ";
    
    while (true) {
      i = iter.next();
      
      str += toString(i, StringType.WITHOUT_ARTICLE);
      
      if (!iter.hasNext()) {
        str += ".";
        break;
      } else {
        str += ", ";
      }
    }
    
    return str;
  }
  
  public static String toNaturalList(Item[] a) {
    return toNaturalList(Arrays.asList(a));
  }
  
  /**
   * Returns a string representation of a list of items, in narrative style,
   * each item being listed with its article.
   * @param a The list of items
   * @return The list's string representation
   */
  public static String toNaturalListWithArticles(List<Item> a) {
    int size = a.size();
    String str = "";
    
    if (size == 0)
      return "No items.";
    
    Iterator<Item> iter = a.iterator();
    
    Item i = iter.next();
    str += capitalize(toString(i, StringType.WITH_ARTICLE));
    
    if (size == 1) {
      return str + ".";
    } else {
      if (size == 2) {
        str += " and ";
      } else {
        str += ", ";
      }
    }

    int count = 2;
    while (iter.hasNext()) {
      i = iter.next();

      str += toString(i, StringType.WITH_ARTICLE);

      if (count == size - 1) {
        if (size == 2) {
          str += " and ";
        } else {
          str += ", and ";
        }
      } else if (count != size) {
        str += ", ";
      }

      count++;
    }

    str += ".";

    return str;
  }
  
  public static String toNaturalListWithArticles(Item[] a) {
    return toNaturalListWithArticles(Arrays.asList(a));
  }
  
  public String narrateMaterialization(String name) {
    Phrase p = getRandomPhrase(MATERIALIZATION_PHRASES);
    p.map(PhraseElement.SUBJECT, name);
    return p.toString();
  }
  
  public String narrateDematerialization(String name) {
    Phrase p = getRandomPhrase(DEMATERIALIZATION_PHRASES);
    p.map(PhraseElement.SUBJECT, name);
    return p.toString();
  }

  public String narrateMoveToRoom(String name, String dest) {
    Phrase p = getRandomPhrase(MOVE_TO_ROOM_PHRASES);
    p.map(PhraseElement.SUBJECT, name);
    p.map(PhraseElement.DESTINATION, dest);
    return p.toString();
  }
  
  public String narrateMoveHere(String name) {
    Phrase p = getRandomPhrase(MOVE_HERE_PHRASES);
    p.map(PhraseElement.SUBJECT, name);
    return p.toString();
  }

  public String narrateMoveInDirection(String name, String dir) {
    Phrase p = getRandomPhrase(MOVE_DIRECTION_PHRASES);
    p.map(PhraseElement.SUBJECT, name);
    p.map(PhraseElement.DIRECTION, dir);
    return p.toString();
  }

  public String narrateTake(String subj, String obj) {
    Phrase p = getRandomPhrase(TAKE_PHRASES);
    p.map(PhraseElement.SUBJECT, subj);
    p.map(PhraseElement.OBJECT, obj);
    return p.toString();
  }

  public String narrateDrop(String subj, String obj) {
    Phrase p = getRandomPhrase(DROP_PHRASES);
    p.map(PhraseElement.SUBJECT, subj);
    p.map(PhraseElement.OBJECT, obj);
    return p.toString();
  }

  public String narrateGive(String subj, String obj, String ind) {
    Phrase p = getRandomPhrase(GIVE_PHRASES);
    p.map(PhraseElement.SUBJECT, subj);
    p.map(PhraseElement.OBJECT, obj);
    p.map(PhraseElement.INDIRECT_OBJECT, ind);
    return p.toString();
  }

  private String narrateSentenceWithVerb(String name, String words, Verb v) {
    int k = getIndexOfClause(words);
    
    String verbString = v.toString();
    char last = words.charAt(words.length() - 1);

    if (k == -1) {
      return name + " " + verbString + ", \"" + words + "\"";
    } else {
      return "\"" + words.substring(0, k + 1) + "\" " + verbString + " " + 
             name + ", \"" + words.substring(k + 2) + "\"";
    }
  }

  public String narrateSay(String name, String words) {
    if (words.isEmpty()) {
      Phrase p = getRandomPhrase(HESITATIONS);
      p.map(PhraseElement.SUBJECT, name);
      return p.toString();
    }

    Verb v;
    if (words.endsWith("?"))
      v = getRandomVerb(INTERROGATIVE_VERBS);
    else
      v = getRandomVerb(SAY_VERBS);
    return narrateSentenceWithVerb(name, words, v);
  }

  public String narrateYell(String name, String words) {
    return narrateSentenceWithVerb(name, words, getRandomVerb(YELL_VERBS));
  }

  public String narrateDistantYell(String words) {
    Phrase p = getRandomPhrase(DISTANT_YELL_PHRASES);
    p.map(PhraseElement.QUOTATION, words);
    return p.toString();
  }

  public String narrateWhisper(String name, String words) {
    return narrateSentenceWithVerb(name, words, getRandomVerb(WHISPER_VERBS));
  }

  public String narrateUnheardWhisper(String who, String whom) {
    Phrase p = getRandomPhrase(WHISPER_OBSERVER_PHRASES);
    p.map(PhraseElement.SUBJECT, who);
    p.map(PhraseElement.OBJECT, whom);
    return p.toString();
  }

  /**
   * Returns the starting index of a clause, or -1 if none was found.
   */
  private int getIndexOfClause(String s) {
    int k = -1;
    
    for (int i = 0; i < CONJUNCTIONS.length; i++) {
      if (CONJUNCTIONS[i].getType() == ConjunctionType.SUBORDINATING) {
        /* Check for inverted conjunction order */
        String word = CONJUNCTIONS[i].getWord();

        if (s.toLowerCase().startsWith(word.toLowerCase())) {
          /* Find first comma */
          k = s.indexOf(',');
        } else {
          /* Subordinate clause does not come first */
          k = s.toLowerCase().indexOf(CONJUNCTIONS[i].toString().toLowerCase());
        }
      } else {
        /* Conjunction is coordinating */
        k = s.toLowerCase().indexOf(CONJUNCTIONS[i].toString().toLowerCase());
      }

      if (k != -1)
        break;
    }

    return k;
  }

  /**
   * Returns a random verb from a specified array of Verb objects.
   */
  private Verb getRandomVerb(Verb[] type) {
    int i = this.r.nextInt(type.length);
    return type[i];
  }

  /**
   * Returns a random phrase from a specified array of Phrase objects.
   */
  private Phrase getRandomPhrase(Phrase[] type) {
    int i = this.r.nextInt(type.length);
    return type[i];
  }

  /**
   * Types of conjunctions, used by the Conjunction class.
   */
  private static enum ConjunctionType {
    COORDINATING, SUBORDINATING
  }

  /**
   * Representation of a conjunction. This class is used by the
   * narrator when a player speaks, so that the narrator can
   * determine where to splice the quotation with a verb.
   */
  private class Conjunction {
    private String word;
    private String form;
    private ConjunctionType type;

    public Conjunction(String s, ConjunctionType t, String f) {
      this.word = s;
      this.type = t;
      this.form = f;
    }

    public Conjunction(String s, ConjunctionType t) {
      this(s, t, ", %s");
    }

    public String toString() {
      return String.format(this.form, this.word);
    }

    public ConjunctionType getType() {
      return this.type;
    }

    public String getWord() {
      return this.word;
    }
  }

  /**
   * Basic English conjunctions used to find clauses in sentences.
   */
  private final Conjunction[] CONJUNCTIONS = {
    /*
     * English subordinating conjunctions
     */
    new Conjunction("after", ConjunctionType.SUBORDINATING),
    new Conjunction("although", ConjunctionType.SUBORDINATING),
    new Conjunction("as", ConjunctionType.SUBORDINATING),
    new Conjunction("as if", ConjunctionType.SUBORDINATING),
    new Conjunction("as long as", ConjunctionType.SUBORDINATING),
    new Conjunction("as much as", ConjunctionType.SUBORDINATING),
    new Conjunction("as soon as", ConjunctionType.SUBORDINATING),
    new Conjunction("as though", ConjunctionType.SUBORDINATING),
    new Conjunction("because", ConjunctionType.SUBORDINATING),
    new Conjunction("before", ConjunctionType.SUBORDINATING),
    new Conjunction("by the time", ConjunctionType.SUBORDINATING),
    new Conjunction("even if", ConjunctionType.SUBORDINATING),
    new Conjunction("even though", ConjunctionType.SUBORDINATING),
    new Conjunction("if", ConjunctionType.SUBORDINATING),
    new Conjunction("in order to", ConjunctionType.SUBORDINATING),
    new Conjunction("in case", ConjunctionType.SUBORDINATING),
    new Conjunction("lest", ConjunctionType.SUBORDINATING),
    new Conjunction("once", ConjunctionType.SUBORDINATING),
    new Conjunction("only if", ConjunctionType.SUBORDINATING),
    new Conjunction("provided", ConjunctionType.SUBORDINATING),
    new Conjunction("since", ConjunctionType.SUBORDINATING),
    new Conjunction("so that", ConjunctionType.SUBORDINATING),
    new Conjunction("though", ConjunctionType.SUBORDINATING),
    new Conjunction("till", ConjunctionType.SUBORDINATING),
    new Conjunction("unless", ConjunctionType.SUBORDINATING),
    new Conjunction("until", ConjunctionType.SUBORDINATING),
    new Conjunction("when", ConjunctionType.SUBORDINATING),
    new Conjunction("whenever", ConjunctionType.SUBORDINATING),
    new Conjunction("where", ConjunctionType.SUBORDINATING),
    new Conjunction("wherever", ConjunctionType.SUBORDINATING),
    new Conjunction("while", ConjunctionType.SUBORDINATING),

    /*
     * English coordinating conjunctions
     */
    new Conjunction("and", ConjunctionType.COORDINATING),
    new Conjunction("but", ConjunctionType.COORDINATING),
    new Conjunction("or", ConjunctionType.COORDINATING),
    new Conjunction("so", ConjunctionType.COORDINATING),
    new Conjunction("yet", ConjunctionType.COORDINATING)
  };

  /**
   * Representation of a verb. Instantiations of this class are used by
   * the narrator when a player speaks.
   */
  private class Verb {
    private String word;
    private boolean splicing;

    public Verb(String word) {
      this.word = word;
      this.splicing = true;
    }

    public String toString() {
      return this.word;
    }
  }

  /**
   * Synonyms for use when a player performs the 'say' action.
   */
  private final Verb[] SAY_VERBS = {
    new Verb("says"),
    new Verb("reports"),
    new Verb("states"),
    new Verb("utters")
  };

  /**
   * Synonyms for use when a player performs the 'say' action and
   * ends the sentence with a question mark.
   */
  private final Verb[] INTERROGATIVE_VERBS = {
    new Verb("asks"),
    new Verb("inquires"),
    new Verb("queries")
  };

  /**
   * Synonyms for use when a player performs the 'yell' action.
   */
  private final Verb[] YELL_VERBS = {
    new Verb("yells"),
    new Verb("shouts"),
    new Verb("shrieks"),
    new Verb("exclaims"),
    new Verb("cries")
  };

  /**
   * Synonyms for use when a player performs the 'whisper' action.
   */
  private final Verb[] WHISPER_VERBS = {
    new Verb("whispers"),
    new Verb("mumbles"),
    new Verb("murmurs"),
    new Verb("mutters")
  };

  /**
   * Types of phrase elements, to be used by the Phrase class below when
   * replacing conversion specifications in a format string.
   */
  private static enum PhraseElement {
    SUBJECT,          // "Martin" in: "Martin walks northeast."
    OBJECT,           // "the key" in: "Peter takes the key."
    INDIRECT_OBJECT,  // "the ball" in: "Greg gives Mia the ball."
    DIRECTION,        // "south" in: "Sarah goes south."
    DESTINATION,      // "the hallway" in: "Sana exits to the hallway."
    QUOTATION,        // "Hey!" in: "Kathryn yells, 'Hey!'"
  }

  /**
   * Representation of a phrase that contains conversion
   * specifications.
   */
  private class Phrase {
    private String phrase;
    private PhraseElement[] elements;
    private EnumMap<PhraseElement, String> mappings;

    public Phrase(String s, PhraseElement... nouns) {
      this.mappings = new EnumMap<PhraseElement, String>(PhraseElement.class);
      this.phrase = s;
      this.elements = nouns;
    }

    public void map(PhraseElement pe, String s) {
      this.mappings.put(pe, s);
    }

    public String toString() {
      if (this.elements.length != this.mappings.size())
        throw new IllegalArgumentException("too few mappings");

      String[] mappedStrings = new String[this.elements.length];

      for (int i = 0; i < this.elements.length; i++) {
        mappedStrings[i] = this.mappings.get(elements[i]);
      }

      return String.format(this.phrase, (Object[])mappedStrings);
    }
  }

  /**
   * Phrases printed when a player observes a player connecting to the
   * server and appearing in the room.
   */
  private final Phrase[] MATERIALIZATION_PHRASES = {
    new Phrase("%s appears out of thin air.", PhraseElement.SUBJECT),
    new Phrase("%s materializes out of thin air.", PhraseElement.SUBJECT),
    new Phrase("%s appears.", PhraseElement.SUBJECT),
    new Phrase("Out of nowhere, %s appears.", PhraseElement.SUBJECT)
  };
  
  /**
   * Phrases printed when a player observes a player disconnecting and
   * disappearing from the room.
   */
  private final Phrase[] DEMATERIALIZATION_PHRASES = {
    new Phrase("%s disappears.", PhraseElement.SUBJECT),
    new Phrase("%s dematerializes.", PhraseElement.SUBJECT),
    new Phrase("In a flash, %s is gone.", PhraseElement.SUBJECT)
  };
  
  /**
   * Phrases printed when a player uses 'say' with no sentence (e.g., 
   * "John remains silent.").
   */
  private final Phrase[] HESITATIONS = {
    new Phrase("%s remains silent.", PhraseElement.SUBJECT),
    new Phrase("%s hesitates to speak.", PhraseElement.SUBJECT),
    new Phrase("%s doesn't say anything.", PhraseElement.SUBJECT),
    new Phrase("%s stays quiet.", PhraseElement.SUBJECT)
  };
  
  /**
   * Phrases printed when another player enters the room the observing
   * player is in.
   */
  private final Phrase[] MOVE_HERE_PHRASES = {
    new Phrase("%s enters.", PhraseElement.SUBJECT),
    new Phrase("%s arrives.", PhraseElement.SUBJECT)
  };

  /**
   * Phrases for use when a player moves into a space whose name other
   * players cannot or are not allowed to see (e.g., "Martina moves
   * northeast.").
   */
  private final Phrase[] MOVE_DIRECTION_PHRASES = {
    new Phrase("%s moves %s.", PhraseElement.SUBJECT,
               PhraseElement.DIRECTION),
    new Phrase("%s walks %s.", PhraseElement.SUBJECT,
               PhraseElement.DIRECTION),
    new Phrase("%s goes %s.", PhraseElement.SUBJECT,
               PhraseElement.DIRECTION),
    new Phrase("%s wanders %s.", PhraseElement.SUBJECT,
               PhraseElement.DIRECTION),
    new Phrase("%s leaves to the %s.", PhraseElement.SUBJECT,
               PhraseElement.DIRECTION),
    new Phrase("%s exits to the %s.", PhraseElement.SUBJECT,
               PhraseElement.DIRECTION)
  };

  /**
   * Phrases printed when a player moves into an adjacent room (e.g.,
   * "Friedrich enters the broom closet.").
   */
  private final Phrase[] MOVE_TO_ROOM_PHRASES = {
    new Phrase("%s walks to %s.", PhraseElement.SUBJECT,
               PhraseElement.DESTINATION),
    new Phrase("%s enters %s.", PhraseElement.SUBJECT,
               PhraseElement.DESTINATION),
    new Phrase("%s goes to %s.", PhraseElement.SUBJECT,
               PhraseElement.DESTINATION),
    new Phrase("%s moves to %s.", PhraseElement.SUBJECT,
               PhraseElement.DESTINATION)
  };

  /**
   * Phrases printed when a player takes an item (e.g., "Lauren takes
   * the broomstick.").
   */
  private final Phrase[] TAKE_PHRASES = {
    new Phrase("%s takes %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT),
    new Phrase("%s picks up %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT),
    new Phrase("%s stashes %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT),
    new Phrase("%s grabs %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT)
  };

  /**
   * Phrases printed when a player gives an item to another player (e.g.,
   * "Stephen gives the cake to Ryan.").
   */
  private final Phrase[] GIVE_PHRASES = {
    new Phrase("%s gives %s to %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT, PhraseElement.INDIRECT_OBJECT),
    new Phrase("%s gives %s %s.", PhraseElement.SUBJECT, 
               PhraseElement.INDIRECT_OBJECT, PhraseElement.OBJECT),
    new Phrase("%s hands %s %s.", PhraseElement.SUBJECT, 
               PhraseElement.INDIRECT_OBJECT, PhraseElement.OBJECT),
    new Phrase("%s lets %s have %s.", PhraseElement.SUBJECT, 
               PhraseElement.INDIRECT_OBJECT, PhraseElement.OBJECT)
  };

  /**
   * Phrases printed when a player drops an item (e.g., "Kenneth leaves
   * the ring.").
   */
  private final Phrase[] DROP_PHRASES = {
    new Phrase("%s drops %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT),
    new Phrase("%s releases %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT),
    new Phrase("%s lets go of %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT),
    new Phrase("%s leaves %s.", PhraseElement.SUBJECT, 
               PhraseElement.OBJECT)
  };

  /**
   * Phrases printed when a player observes one player whispering to
   * another (e.g., "Jason whispers to Raymond.").
   */
  private final Phrase[] WHISPER_OBSERVER_PHRASES = {
    new Phrase("%s whispers in %s ear.", PhraseElement.SUBJECT,
               PhraseElement.OBJECT),
    new Phrase("%s mutters something to %s.", PhraseElement.SUBJECT,
               PhraseElement.OBJECT),
    new Phrase("%s whispers to %s.", PhraseElement.SUBJECT,
               PhraseElement.OBJECT),
    new Phrase("%s mumbles something only %s can hear.", PhraseElement.SUBJECT,
               PhraseElement.OBJECT),
    new Phrase("%s tells %s something, but you are unable to hear it.",
               PhraseElement.SUBJECT, PhraseElement.OBJECT),
    new Phrase("%s speaks softly to %s.", PhraseElement.SUBJECT,
               PhraseElement.OBJECT),
    new Phrase("%s says something to %s.", PhraseElement.SUBJECT,
               PhraseElement.OBJECT)
  };

  /**
   * Phrases printed when there is player movement in adjacent rooms.
   */
  private final Phrase[] DISTANT_MOVEMENT_PHRASES = {
    new Phrase("Footsteps in the distance."),
    new Phrase("Footsteps."),
    new Phrase("Distant footsteps."),
    new Phrase("There is movement nearby."),
    new Phrase("Movement can be heard."),
    new Phrase("Someone's moving."),
    new Phrase("Sounds of movement.")
  };

  /**
   * Phrases printed when a faraway player yells.
   */
  private final Phrase[] DISTANT_YELL_PHRASES = {
    new Phrase("Someone in the distance yells, \"%s\"",
               PhraseElement.QUOTATION),
    new Phrase("Someone shouts, \"%s\"", PhraseElement.QUOTATION),
    new Phrase("Someone yells, \"%s\"", PhraseElement.QUOTATION),
    new Phrase("In the distance someone shouts, \"%s\"", 
               PhraseElement.QUOTATION),
    new Phrase("\"%s\" from a distance.",
               PhraseElement.QUOTATION)
  };

}
