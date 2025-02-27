package cargo.kityk.wms.order

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/orders")
class OrderController {

    @PostMapping
    ResponseEntity<Void> createOrder(@RequestBody(required = true) String order) {
        return new ResponseEntity<Void>(HttpStatus.CREATED)
    }

    @GetMapping
    ResponseEntity<String> getOrders() {
        return ResponseEntity.ok("[]")
    }

    @GetMapping("/{id}")
    ResponseEntity<String> getOrder(@PathVariable("id") String id) {
        return ResponseEntity.ok("{}")
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> updateOrder(@PathVariable("id") String id, @RequestBody(required = true) String order) {
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteOrder(@PathVariable("id") String id) {
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/allocate")
    ResponseEntity<Void> allocateInventory(@PathVariable("id") String id) {
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{id}/status")
    ResponseEntity<Void> updateOrderStatus(@PathVariable("id") String id, @RequestBody(required = true) String status) {
        return ResponseEntity.ok().build()
    }
}
