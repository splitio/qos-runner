package io.split.qos.server.integrations.slack.commands;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SlackCommonFormatterTest {

    private SlackCommonFormatter formatter;

    @Before
    public void setUp() {
        formatter = new SlackCommonFormatter("5");
    }

    @Test
    public void testGrouping() {
        List<String> result = formatter.groupMessage(Lists.newArrayList("one"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("```\n" +
                            "one\n" +
                            "```\n",
                            result.get(0));
        result = formatter.groupMessage(Lists.newArrayList("one", "two"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("```\n" +
                        "one\n" +
                        "two\n" +
                        "```\n",
                result.get(0));
        result = formatter.groupMessage(Lists.newArrayList("one", "two", "three", "four", "five"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("```\n" +
                        "one\n" +
                        "two\n" +
                        "three\n" +
                        "four\n" +
                        "five\n" +
                        "```\n",
                result.get(0));
        result = formatter.groupMessage(Lists.newArrayList("one", "two", "three", "four", "five", "six"));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("```\n" +
                        "one\n" +
                        "two\n" +
                        "three\n" +
                        "four\n" +
                        "five\n" +
                        "```\n",
                result.get(0));
        Assert.assertEquals("```\n" +
                        "six\n" +
                        "```\n",
                result.get(1));
        result = formatter.groupMessage(Lists.newArrayList("one", "two", "three", "four", "five",
                                                           "six", "seven"));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("```\n" +
                        "one\n" +
                        "two\n" +
                        "three\n" +
                        "four\n" +
                        "five\n" +
                        "```\n",
                result.get(0));
        Assert.assertEquals("```\n" +
                        "six\n" +
                        "seven\n" +
                        "```\n",
                result.get(1));
        result = formatter.groupMessage(Lists.newArrayList("one", "two", "three", "four", "five",
                                                           "six", "seven", "eight", "nine", "ten",
                                                           "eleven"));
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("```\n" +
                        "one\n" +
                        "two\n" +
                        "three\n" +
                        "four\n" +
                        "five\n" +
                        "```\n",
                result.get(0));
        Assert.assertEquals("```\n" +
                        "six\n" +
                        "seven\n" +
                        "eight\n" +
                        "nine\n" +
                        "ten\n" +
                        "```\n",
                result.get(1));
        Assert.assertEquals("```\n" +
                        "eleven\n" +
                        "```\n",
                result.get(2));
    }
}
