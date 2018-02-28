package dk.mzw.grow



case class Tree(
    branches : List[(Double,  Tree)]
)

case class Branch(
    angle : Double,
    age : Double,
    tree : Tree
)