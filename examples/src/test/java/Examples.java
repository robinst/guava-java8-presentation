import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.Files;
import com.google.common.math.IntMath;
import com.google.common.net.UrlEscapers;

public class Examples {

	private final Logger logger = Logger.getLogger(Examples.class.getName());

	@Test
	public void strings() {
		// Guava
		String s = getString();
		Strings.isNullOrEmpty(s);
		Strings.nullToEmpty(s);
		Strings.repeat("-", 70);

		List<String> parts = Arrays.asList("a", "b", "c", null);
		String joined = Joiner.on(", ").skipNulls().join(parts);
		assertThat(joined, is("a, b, c"));

		MapSplitter splitter = Splitter.on(" ").withKeyValueSeparator(":");
		splitter.split("a:1 b:2"); // => Map {a=1, b=2}
	}

	@Test
	public void collections() {
		List<Person> persons = getPersons();

		// Guava
		Iterable<Person> adults = Iterables.filter(persons,
				new Predicate<Person>() {
					@Override
					public boolean apply(Person p) {
						return p.getAge() >= 18;
					}
				});
		List<String> names = Lists.newArrayList(Iterables.transform(adults,
				new Function<Person, String>() {
					@Override
					public String apply(Person p) {
						return p.getName();
					}
				}));

		// Guava
		FluentIterable.from(persons)
				.filter(new Predicate<Person>() {
					@Override
					public boolean apply(Person p) {
						return p.getAge() >= 18;
					}
				})
				.transform(new Function<Person, String>() {
					@Override
					public String apply(Person p) {
						return p.getName();
					}
				})
				.toList();

		// Guava & Java 8
		FluentIterable.from(persons)
				.filter((Person p) -> {
					return p.getAge() >= 18;
				})
				.transform((Person p) -> {
					return p.getName();
				})
				.toList();

		// Guava & Java 8
		FluentIterable.from(persons)
				.filter((Person p) -> p.getAge() >= 18)
				.transform((Person p) -> p.getName())
				.toList();

		// Guava & Java 8
		FluentIterable.from(persons)
				.filter(p -> p.getAge() >= 18)
				.transform(p -> p.getName())
				.toList();

		Predicate<Person> predicate = p -> p.getAge() >= 18;

		Comparator<Person> comparator = (a, b) -> a.getName().compareTo(b.getName());

		Runnable runnable = () -> System.out.println("Hello World");

		// Java 8 Streams
		persons.stream()
				.filter(p -> p.getAge() >= 18)
				.map(p -> p.getName())
				.collect(Collectors.toList());

		ArrayList<String> parts = getParts();
		parts.stream().collect(Collectors.joining(", "));

		Double averageAge = persons.stream()
				.collect(Collectors.averagingInt(p -> p.getAge()));

		Map<Integer, List<Person>> personByAge = persons.stream()
				.collect(Collectors.groupingBy(p -> p.getAge()));
	}

	@Test
	public void comparing() {
		// Guava
		Ordering.natural()
				.onResultOf(new Function<Person, String>() {
					public String apply(Person input) {
						return input.getName();
					}
				})
				.compound(Ordering.natural()
						.onResultOf(new Function<Person, Integer>() {
							public Integer apply(Person input) {
								return input.getAge();
							}
						}));

		// Java 8
		Comparator
				.comparing(Person::getName)
				.thenComparing(Person::getAge);
	}

	@Test
	public void range() {
		LocalDate first = LocalDate.of(2014, 1, 1);
		LocalDate last = LocalDate.of(2014, 1, 31);
		Range<LocalDate> range = Range.closed(first, last);
		Range<LocalDate> part = Range.closed(first, LocalDate.of(2014, 1, 12));
		assertThat(range.contains(last.plusDays(1)), is(false));
		assertThat(range.encloses(part), is(true));

		// see RangeMap, RangeSet
	}

	@Test
	public void io() throws IOException {
		List<String> lines = Files.readLines(new File("foo.txt"), Charsets.UTF_8);

		Files.copy(new File("foo.txt"), new File("bar.txt"));

		FluentIterable<File> traversal = Files.fileTreeTraverser().postOrderTraversal(
				new File("dir"));

		Stream<String> stream = java.nio.file.Files.lines(new File("foo.txt").toPath(),
				Charset.forName("UTF-8"));

		java.nio.file.Files.copy(new File("foo.txt").toPath(), new File("bar.txt").toPath(),
				StandardCopyOption.REPLACE_EXISTING);

		try (Stream<Path> paths = java.nio.file.Files.walk(new File("dir").toPath())) {
			// ...
		}
	}

	@Test
	public void optional() {
		java.util.Optional<Person> optional = getPerson();

		String name = optional.map(p -> p.getName()).orElse("Unknown");

		String parentName = optional.flatMap(p -> p.getParent())
				.map(p -> p.getName())
				.orElse("Unknown Parent");
	}

	@Test
	public void stopwatch() {
		Stopwatch stopwatch = Stopwatch.createStarted();
		doSomething();
		stopwatch.stop();
		logger.info("Do something took " + stopwatch); // => 4.200 s
	}

	@Test
	public void math() {
		assertThat(IntMath.checkedPow(2, 10), is(1024));
		assertThat(IntMath.divide(7, 2, RoundingMode.HALF_UP), is(4));
	}

	@Test
	public void mixed() throws IOException {
		Optional<RoundingMode> roundingMode = Enums.getIfPresent(RoundingMode.class, "HALF_UP");

		Iterator<String> iterator = getIterator();
		String[] elements = Iterators.toArray(iterator, String.class);

		// Guava 15.0:
		assertThat(HtmlEscapers.htmlEscaper().escape("1 < 2"), is("1 &lt; 2"));
		assertThat(UrlEscapers.urlFormParameterEscaper().escape("hi\nthere"), is("hi%0Athere"));
	}

	@Test
	public void returning() {
		List<String> list = Arrays.asList("a", "b", "c");
		list.stream().filter(s -> {
			return s.equals("a");
		});
	}

	private String getString() {
		return "";
	}

	private Iterator<String> getIterator() {
		return Arrays.asList("a", "b").iterator();
	}

	private void doSomething() {
		try {
			Thread.sleep(4200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private java.util.Optional<Person> getPerson() {
		return java.util.Optional.of(new Person("Foo", 20));
	}

	private ArrayList<String> getParts() {
		return new ArrayList<>();
	}

	private List<Person> getPersons() {
		return Arrays.asList(new Person("Foo", 20), new Person("Bar", 15));
	}

	public static class Person {
		private final String name;
		private final int age;
		private java.util.Optional<Person> parent = java.util.Optional.empty();

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public java.util.Optional<Person> getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return name + " " + age;
		}
	}
}
