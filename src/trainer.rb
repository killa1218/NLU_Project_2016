require File.dirname(__FILE__)+'/connective'
require File.dirname(__FILE__)+'/arg_pos'
require File.dirname(__FILE__)+'/arg_ext_ss'
require File.dirname(__FILE__)+'/arg_ext_ps'
require File.dirname(__FILE__)+'/explicit'
require File.dirname(__FILE__)+'/implicit'
require File.dirname(__FILE__)+'/attribution'
require File.dirname(__FILE__)+'/corpus'
require File.dirname(__FILE__)+'/variable'

class Trainer
  def train
    cp = ".:#{File.dirname(__FILE__)}/../lib/maxent-2.5.2/lib/trove.jar:#{File.dirname(__FILE__)}/../lib/maxent-2.5.2/output/maxent-2.5.2.jar:#{File.dirname(__FILE__)}/../lib/opennlp-tools-1.3.0/output/opennlp-tools-1.3.0.jar:#{File.dirname(__FILE__)}/../lib/opennlp-tools-1.3.0/lib/jwnl-1.3.3.jar"
    $prule_file  = File.dirname(__FILE__)+'/../lib/wsj.rule.txt'
    $drule_file  = File.dirname(__FILE__)+'/../lib/wsj.dtree.txt'
    $wordpair_file  = File.dirname(__FILE__)+'/../lib/wsj.word-pair.txt'
    data_sets    = %w/00 01 02 03 04 05 06 07 08 09 10 11 12 13/

    puts data_sets

    data_sets.each do |t|
      $train_data = Variable::All_data - [t]
      prefix = 'leave1out.'+t

      if not File.exist?('../data/'+prefix+'.conn.nep.npp.train') then
        puts '../data/'+prefix+'.conn.nep.npp.train'
        obj = Connective.new
        obj.prepare_data(prefix+'.conn.nep.npp', true)
        res = `cd #{File.dirname(__FILE__)}/../eval; java -cp #{cp} CreateModel -real ../data/#{prefix}.conn.nep.npp.train`
        puts res
        exit
      elsif not File.exist?('../data/'+prefix+'.argpos.nep.npp.train') then
        puts '../data/'+prefix+'.argpos.nep.npp.train'
        obj = ArgPos.new
        obj.prepare_data(prefix+'.argpos.nep.npp', true)
        res = `cd #{File.dirname(__FILE__)}/../eval; java -cp #{cp} CreateModel -real ../data/#{prefix}.argpos.nep.npp.train`
        puts res
        exit
      elsif not File.exist?('../data/'+prefix+'.argextss.nep.npp.train') then
        puts '../data/'+prefix+'.argextss.nep.npp.train'
        obj = ArgExtSS.new
        obj.prepare_data(prefix+'.argextss.nep.npp', true)
        res = `cd #{File.dirname(__FILE__)}/../eval; java -cp #{cp} CreateModel -real ../data/#{prefix}.argextss.nep.npp.train`
        puts res
        exit
      elsif not File.exist?('../data/'+prefix+'.exp.nep.npp.train') then
        puts '../data/'+prefix+'.exp.nep.npp.train'
        obj = Explicit.new
        obj.prepare_data(prefix+'.exp.nep.npp', true)
        res = `cd #{File.dirname(__FILE__)}/../eval; java -cp #{cp} CreateModel -real ../data/#{prefix}.exp.nep.npp.train`
        puts res
        exit
      elsif not File.exist?('../data/'+prefix+'.nonexp.nep.npp.train') then
        puts '../data/'+prefix+'.nonexp.nep.npp.train'
        obj = Implicit.new(100, 100, 500, false)
        obj.prepare_data(prefix+'.nonexp.nep.npp', true)
        res = `cd #{File.dirname(__FILE__)}/../eval; java -cp #{cp} CreateModel -real ../data/#{prefix}.nonexp.nep.npp.train`
        puts res
        exit
      elsif not File.exist?('../data/'+prefix+'.attr.nep.npp.train') then
        puts '../data/'+prefix+'.attr.nep.npp.train'
        obj = Attribution.new
        obj.prepare_data(prefix+'.attr.nep.npp', true)
        res = `cd #{File.dirname(__FILE__)}/../eval; java -cp #{cp} CreateModel -real ../data/#{prefix}.attr.nep.npp.train`
        puts res
        exit
      end
    end
  end
end
