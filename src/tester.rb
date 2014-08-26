require File.dirname(__FILE__)+'/parser'

class Tester
  # Takes in Arrays expected and predicted containing the results for each and
  # returns the confusion matrix, a 2d array with rows sorted in alphabetical
  # order of the classes
  def confusion_matrix(expected, predicted)
    # Assume that expected contains all the classes
    classes = expected.uniq.sort
    num_class = classes.length
    matrix = Array.new(num_class) { Array.new(num_class, 0) }

    expected.each_with_index { |s, i|
      j = classes.find_index(s)
      if (s == predicted[i])
        matrix[j][j] += 1
      else
        k = classes.find_index(predicted[i])
        matrix[j][k] += 1
      end
    }
    return matrix.insert(0, classes)
  end

  # Wrapper function to pretty print the confusion matrix
  def print_confusion_matrix(expected, predicted)
    matrix = confusion_matrix(expected, predicted)
    puts matrix[0].to_s
    matrix.each_with_index { |r, i|
      if i > 0
        puts r.to_s + [matrix[0][i - 1]].to_s
      end
    }
  end
end
