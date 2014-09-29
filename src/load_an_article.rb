#!/usr/bin/ruby

require File.dirname(__FILE__)+'/parser'

parser = Parser.new
article = parser.parse_text("#{Dir.pwd}/../test2_wsj_2309")
