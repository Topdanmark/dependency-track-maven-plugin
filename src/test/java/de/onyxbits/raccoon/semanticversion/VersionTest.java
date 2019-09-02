package de.onyxbits.raccoon.semanticversion;

import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;

import org.junit.Assert;

public class VersionTest {

    @Test
    public void testEquality() throws ParseException {
        SemanticVersion v1 = new SemanticVersion("1.2.3-alpha.1+build.2");
        SemanticVersion v2 = new SemanticVersion("1.2.3-alpha.1+build.2");
        Assert.assertEquals(v1, v2);
    }

    @Test(expected = ParseException.class)
    public void testWontParse() throws ParseException {
        new SemanticVersion("won't parse");
    }

    @Test(expected = ParseException.class)
    public void testBroken() throws ParseException {
        new SemanticVersion("1..2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIdentifiers() {
        String[] ids = {
                "ok", "not_ok"
        };
        new SemanticVersion(0, 0, 0, ids, ids);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutOfBounds() {
        new SemanticVersion(-1, 0, 0);
    }

    @Test
    public void testParsePlain() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3");
        Assert.assertEquals(1, v.major);
        Assert.assertEquals(2, v.minor);
        Assert.assertEquals(3, v.patch);
        Assert.assertEquals("1.2.3", v.toString());

        v = new SemanticVersion("11.22.33");
        Assert.assertEquals(11, v.major);
        Assert.assertEquals(22, v.minor);
        Assert.assertEquals(33, v.patch);
        Assert.assertEquals("11.22.33", v.toString());
    }

    @Test
    public void testParseRelease() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3-alpha.1");
        Assert.assertEquals(1, v.major);
        Assert.assertEquals(2, v.minor);
        Assert.assertEquals(3, v.patch);
        Assert.assertEquals("alpha", v.preRelease[0]);
        Assert.assertEquals("1.2.3-alpha.1", v.toString());
    }

    @Test
    public void testParseMeta() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3+build.1");
        Assert.assertEquals(1, v.major);
        Assert.assertEquals(2, v.minor);
        Assert.assertEquals(3, v.patch);
        Assert.assertEquals("build", v.buildMeta[0]);
        Assert.assertEquals("1.2.3+build.1", v.toString());
    }

    @Test
    public void testParseReleaseMeta() throws ParseException {
        SemanticVersion v = new SemanticVersion("1.2.3-alpha.1+build.1");
        Assert.assertEquals(1, v.major);
        Assert.assertEquals(2, v.minor);
        Assert.assertEquals(3, v.patch);
        Assert.assertEquals("alpha", v.preRelease[0]);
        Assert.assertEquals("build", v.buildMeta[0]);
        Assert.assertEquals("1.2.3-alpha.1+build.1", v.toString());
    }

    @Test
    public void testNewer() {
        SemanticVersion[] inOrder = {
                new SemanticVersion(0, 1, 4), new SemanticVersion(1, 1, 1),
                new SemanticVersion(1, 2, 1), new SemanticVersion(1, 2, 3)
        };

        SemanticVersion[] wrongOrder = {
                inOrder[0], inOrder[3], inOrder[1], inOrder[2]
        };

        Arrays.sort(wrongOrder);
        Assert.assertArrayEquals(inOrder, wrongOrder);
    }

    @Test
    public void testUpdate() {
        Assert.assertTrue(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(1,
                1, 0)));
        Assert.assertFalse(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(1,
                1, 2)));
        Assert.assertFalse(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(1,
                1, 1)));

        Assert.assertTrue(new SemanticVersion(1, 1, 2)
                .isCompatibleUpdateFor(new SemanticVersion(1, 1, 1)));
        Assert.assertFalse(new SemanticVersion(2, 1, 1)
                .isCompatibleUpdateFor(new SemanticVersion(1, 1, 0)));
    }

    @Test
    public void testPreRelease() throws ParseException {
        Assert.assertTrue(new SemanticVersion(1, 1, 1).isUpdateFor(new SemanticVersion(
                "1.1.1-alpha")));
        Assert.assertFalse(new SemanticVersion("1.1.1-alpha")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha")));
        Assert.assertFalse(new SemanticVersion("1.1.1-alpha.1")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha.1.1")));
        Assert.assertTrue(new SemanticVersion("1.1.1-alpha.1.one")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha.1.1")));
        Assert.assertTrue(new SemanticVersion("1.1.1-alpha.1.one")
                .isUpdateFor(new SemanticVersion("1.1.1-alpha.1.1.1")));
    }

    @Test
    public void testSortList() throws ParseException {
        //"1.9.99", "1.9.90", "1.9.3", "1.9.2", "1.6.62", "1.10.0"
        //"1.6.62", "1.9.2", "1.9.3", "1.9.90", "1.9.99", "1.10.0"
		SemanticVersion[] expected = {new SemanticVersion("1.6.2"),
				new SemanticVersion("1.9.2"),
				new SemanticVersion("1.9.3"),
				new SemanticVersion("1.9.99-SNAPSHOT"),
				new SemanticVersion("1.9.99"),
				new SemanticVersion("1.10.0")};
        SemanticVersion[] actual = {new SemanticVersion("1.9.99"),
				new SemanticVersion("1.9.99-SNAPSHOT"),
				new SemanticVersion("1.9.3"),
				new SemanticVersion("1.9.2"),
				new SemanticVersion("1.6.2"),
				new SemanticVersion("1.10.0")};
        Arrays.sort(actual);
        Assert.assertArrayEquals(expected, actual);
    }
}
