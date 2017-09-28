package dk.mzw.guts.entities

import dk.mzw.guts.entities.PlayerEntity.{SetXVelocity, SetYVelocity}
import dk.mzw.guts.entities.GutsWorldEntity.{SpawnCorpse, SpawnFlame, SpawnTurret, Unspawn}
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.pyroman.Keys
import dk.mzw.scalasprites.SpriteCanvas
import dk.mzw.scalasprites.SpriteCanvas.Image

import scala.util.Random

class PlayerEntity(
    val world : WorldEntity,
    val self : Self,
    val position : Vector2d,
    val walkingImage : Double => Image,
    val shootingImage : Double => Image
) extends Entity
    with PawnEntity
    with CollidingEntity
    with ControlledEntity
    with ReceivingEntity
    with UpdateableEntity
    with HittableEntity
    with DrawableEntity
    with MortalEntity
{

    var health = 100
    val speed = 4
    val size = Vector2d(0.8, 0.8)
    val velocity = Vector2d(0, 0)
    var angle : Double = Math.PI * 0.5
    var shooting = false
    var walkingDistance : Double = 0
    val collision = Collision()

    // Reserved registers
    val previousPosition = Vector2d(0, 0)

    var space = false
    var enter = false
    var keyX : Int = 0
    var keyY : Int = 0

    override def onInput(world : WorldEntity, keys : Keys) : Unit = {
        space = keys(Keys.space)
        if(!enter && keys(Keys.enter)) {
            sendMessageTo(world, SpawnTurret(Self(), position.copy(), angle))
        }
        enter = keys(Keys.enter)

        val newKeyX = keys.factor(Keys.leftArrow, Keys.rightArrow)
        val newKeyY = keys.factor(Keys.downArrow, Keys.upArrow)
        if(newKeyX != keyX || newKeyY != keyY) {
            keyX = newKeyX
            keyY = newKeyY
            val l = Math.sqrt(keyX * keyX + keyY * keyY)
            val f = if(l > 0) 1 / l * speed else 0
            sendMessageTo(this, SetXVelocity(position.x, keyX * f))
            sendMessageTo(this, SetYVelocity(position.y, keyY * f))
        }
    }

    override def onMessage(message : Entity.Message) : Unit = message match {
        case SetXVelocity(x, vx) => position.x = x; velocity.x = vx
        case SetYVelocity(y, vy) => position.y = y; velocity.y = vy
        case _ => super.onMessage(message)
    }

    override def onDie() : Unit = {
        sendMessageTo(world, Unspawn(self))
        val image = walkingImage(walkingDistance)
        sendMessageTo(world, SpawnCorpse(Self(), position, velocity.angle, 1, image))
    }

    override def onUpdate(world : WorldEntity, delta : Double) : Unit = {
        if(space) {
            val shotCount = Math.round(delta * 100).toInt
            for(_ <- 0 until shotCount) {
                val velocityUnit = Vector2d(Math.cos(angle), Math.sin(angle))
                val gunSide = temporary
                gunSide.set(velocityUnit.y * 0.2, -velocityUnit.x * 0.2)
                val p = position.copy()
                velocityUnit.multiply(0.5)
                p.add(velocityUnit)
                p.add(gunSide)
                val a = (Random.nextDouble() - 0.5) * 0.2 + angle
                val s = Math.max(velocity.magnitude * 3, speed * 2)
                sendMessageTo(world, SpawnFlame(Self("flame-" + Math.random(), Entity.localClientId), p, a, s))
            }
        }

        temporary.set(velocity)
        previousPosition.set(position)
        move(world, position, size, temporary, delta, collision)

        if(velocity.magnitude == 0) {
             walkingDistance = 0
        } else {
            temporary.set(position)
            temporary.addMultiplied(previousPosition, -1)
            walkingDistance = walkingDistance + temporary.magnitude
        }

        if(velocity.magnitude != 0) {
            val velocityAngle = velocity.angle
            val da = Math.atan2(Math.sin(velocityAngle - angle), Math.cos(velocityAngle - angle))
            angle = angle + da * delta * 5
        }

    }

    override def onDraw(display : SpriteCanvas.Display) : Unit = {
        val image = if(shooting) shootingImage(walkingDistance) else walkingImage(walkingDistance)
        display.add(image, position.x, position.y, 0.8, angle - Math.PI * 0.5)
    }
}

object PlayerEntity {
    case class SetXVelocity(x : Double, vx : Double) extends Entity.Message
    case class SetYVelocity(y : Double, vy : Double) extends Entity.Message
}