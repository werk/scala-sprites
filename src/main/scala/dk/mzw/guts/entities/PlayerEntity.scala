package dk.mzw.guts.entities

import dk.mzw.guts.Guts
import dk.mzw.guts.entities.GutsWorldEntity._
import dk.mzw.guts.entities.PlayerEntity.{SetXVelocity, SetYVelocity}
import dk.mzw.guts.system.CollidingEntity.Collision
import dk.mzw.guts.system.Entity.Self
import dk.mzw.guts.system._
import dk.mzw.guts.utility.{Mouse, Normal}
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

    var lastSecondaryFire = 0d
    val shotgunCoolDown = 1d

    var lastTertiaryFire = 0d
    val laserCoolDown = 3d


    // Reserved registers
    val previousPosition = Vector2d(0, 0)
    val gunPosition = Vector2d(0, 0)

    var primaryFire = false
    var secondaryFire = false
    var tertiaryFire = false
    var enter = false
    var keyX : Int = 0
    var keyY : Int = 0

    override def onInput(world : WorldEntity, keys : Keys, mouse : Mouse) : Unit = {
        primaryFire = mouse.left
        secondaryFire = keys(Keys.q)
        tertiaryFire = keys(Keys.e)

        // TODO should this be a message ?
        angle = Math.atan2(mouse.y - position.y, mouse.x - position.x)

        if(!enter && keys(Keys.enter)) {
            sendMessageTo(world, SpawnTurret(Self(), position.copy(), angle))
        }
        enter = keys(Keys.enter)

        val newKeyX = keys.factor(Keys.a, Keys.d)
        val newKeyY = keys.factor(Keys.s, Keys.w)
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
        val vx = Math.cos(angle)
        val vy = Math.sin(angle)
        gunPosition.set(position)
        gunPosition.add(vx * 0.5 + vy * 0.2, vy * 0.5 - vx * 0.2)

        if(primaryFire) {
            val shotCount = Math.round(delta * 100).toInt
            for(_ <- 0 until shotCount) {
                val a = (Random.nextDouble() - 0.5) * 0.2 + angle
                val s = Math.max(velocity.magnitude * 3, speed * 2)
                sendMessageTo(world, SpawnFlame(Self("flame-" + Math.random(), Entity.localClientId), gunPosition.copy(), a, s))
            }
        }

        val now = Guts.secondsElapsed()
        if(secondaryFire && (now - lastSecondaryFire) >= shotgunCoolDown) {
            lastSecondaryFire = now
            val shotCount = 100
            for(_ <- 0 until shotCount) {
                val a = angle + Normal() * 0.1
                val s = velocity.magnitude + 15 + Normal()
                sendMessageTo(world, SpawnPellet(Self(), gunPosition.copy(), a, s))
            }
        }

        if(tertiaryFire && (now - lastTertiaryFire) >= laserCoolDown) {
            lastTertiaryFire = now
            sendMessageTo(world, SpawnLaserBeam(Self(), self.id))
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